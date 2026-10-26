package godot.navigation

import godot.annotation.Export
import godot.annotation.Script
import godot.annotation.Tool
import godot.api.*
import godot.core.PackedInt32Array
import godot.core.PackedVector3Array
import godot.core.VariantArray
import godot.core.Vector3
import godot.core.variantArrayOf
import godot.global.GD
import org.jgrapht.alg.util.UnorderedPair
import kotlin.math.min

@Tool
@Script
class SimpleNavGraph : Resource() {
    @Export
    var points: PackedVector3Array = PackedVector3Array()

    @Export
    var connections: PackedInt32Array = PackedInt32Array()

    fun intoAStar(): AStar3D =
        AStar3D().apply {
            reserveSpace(points.size.toLong())

            points.forEachIndexed { idx, vec ->
                addPoint(idx.toLong(), vec)
            }

            (0 until connections.size step 2).forEach { idx ->
                connectPoints(connections[idx].toLong(), connections[idx + 1].toLong())
            }
        }

    companion object {
        private const val VERTS_PER_POLY = 3

        fun createMeshFromNavmesh(mesh: NavigationMesh): ArrayMesh {
            GD.print("Generating mesh from navmesh at ${mesh.resourcePath}...")

            val vertexBuffer = mesh.getVertices()
            val indexBuffer = PackedInt32Array().apply {
                resize(mesh.getPolygonCount() * VERTS_PER_POLY)
            }

            // Collect polygon indices into one buffer
            (0 until mesh.getPolygonCount()).forEach { poly ->
                mesh.getPolygon(poly).forEachIndexed { idx, x ->
                    indexBuffer[(poly * 3) + idx] = x
                }
            }

            // Create our first surface array
            val surfaceArr = VariantArray<Any?>().apply {
                resize(Mesh.ArrayType.MAX.ordinal)
                set(Mesh.ArrayType.VERTEX.ordinal, vertexBuffer)
                set(Mesh.ArrayType.INDEX.ordinal, indexBuffer)
            }

            // Use the importer to create a LOD to simplify our mesh for us
            // Because god knows I ain't doin that myself
            val im = ImporterMesh().apply {
                addSurface(Mesh.PrimitiveType.TRIANGLES, surfaceArr)
                generateLods(20.0F, 0.0F, variantArrayOf())
            }
            // If we generated any lods, we get the simplest one.
            // If the navmesh is super simple, it won't generate anything. I think.
            // So we just use the existing buffer.
            val lodIdxBuffer = if (im.getSurfaceLodCount(0) > 0) im.getSurfaceLodIndices(
                0,
                im.getSurfaceLodCount(0) - 1
            )
            else indexBuffer

            // The LOD is just an index buffer using the same vtx data
            // so we just pass in the same vertex buffer with the new index buffer
            val lodArr = VariantArray<Any?>().apply {
                resize(Mesh.ArrayType.MAX.ordinal)
                set(Mesh.ArrayType.VERTEX.ordinal, vertexBuffer)
                set(Mesh.ArrayType.INDEX.ordinal, lodIdxBuffer)
            }

            return ArrayMesh().apply { addSurfaceFromArrays(Mesh.PrimitiveType.TRIANGLES, lodArr) }
        }

        fun createFromMesh(mesh: ArrayMesh): SimpleNavGraph {
            /*
            1. collect neighbors for each face by getting verts, and then getting vert polygons that aren't that polygon
            2. Collect barycenters for each polygon
            3. Create connections between neighboring polygons
            */
            GD.print("Building graph from mesh...")

            val meshTool = MeshDataTool()
            meshTool.createFromSurface(mesh, 0)

            // Maps to polygon index
            val polyRange = 0.until(meshTool.getFaceCount())

            // Calculate barycenters of each polygon
            val barycenters = polyRange.map { poly ->
                0.until(VERTS_PER_POLY) // vertex indices
                    .fold(Vector3.ZERO) { accum, vert ->
                        accum + meshTool.getVertex(
                            meshTool.getFaceVertex(
                                poly,
                                vert
                            )
                        )
                    } // Get vertex point from index
                    .div(3.0) // Divide into average
            }

            // Collect a list of neighbors for each polygon
            val neighbors = polyRange.map { poly ->
                0.until(VERTS_PER_POLY) // 3 verts
                    .flatMap { vert ->
                        meshTool.getEdgeFaces(
                            meshTool.getFaceEdge(
                                poly,
                                vert
                            )
                        )
                    } // Get polys connected to verts edges into a flat array
                    .filterNot { it == poly } // Get rid of all the ones that are this polygon
            }

            // Add bidirectional connections to a set, so each is unique
            val connectedSet: MutableSet<UnorderedPair<Int, Int>> = mutableSetOf()
            connectedSet.addAll(neighbors.flatMapIndexed { poly, nbrs ->
                nbrs.map { nbr ->
                    UnorderedPair(
                        poly,
                        nbr
                    )
                }
            })

            // Flatten to indices, return navgraph
            return SimpleNavGraph().apply {
                points = PackedVector3Array(barycenters)
                connections = PackedInt32Array(connectedSet.flatMap { p -> listOf(p.first, p.second) })
            }.also { GD.print("Done.") }
        }

        fun createFromNavMesh(nav: NavigationMesh): SimpleNavGraph = createFromMesh(createMeshFromNavmesh(nav))
    }
}
