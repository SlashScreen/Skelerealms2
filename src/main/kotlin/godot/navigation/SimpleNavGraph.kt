package godot.navigation

import godot.annotation.Export
import godot.annotation.Script
import godot.api.ArrayMesh
import godot.api.ImporterMesh
import godot.api.Mesh
import godot.api.MeshDataTool
import godot.api.NavigationMesh
import godot.api.Resource
import godot.core.*
import org.jgrapht.alg.util.UnorderedPair

@Script
class SimpleNavGraph : Resource() {
    @Export
    var doorInstances: VariantArray<DoorInstance> = variantArrayOf()

    @Export
    var weights: Dictionary<StringName, Dictionary<StringName, Float>> = Dictionary()

    @Export
    var points: PackedVector3Array = PackedVector3Array()

    @Export
    var connections: PackedInt32Array = PackedInt32Array()

    companion object {
        fun createMeshFromNavmesh(mesh: NavigationMesh): ArrayMesh {
            val vertexBuffer = mesh.getVertices()
            val indexBuffer = PackedInt32Array()
            indexBuffer.resize(mesh.getPolygonCount() * 3) // TODO: do not hardcode this?

            // Collect polygon indices into one buffer
            (0..mesh.getPolygonCount()).forEach { i ->
                mesh.getPolygon(i).forEachIndexed { idx, x ->
                    indexBuffer[i + idx] = x
                }
            }

            // Create our first surface array
            val surfaceArr = VariantArray<Any?>()
            surfaceArr.resize(Mesh.ArrayType.MAX.ordinal)
            surfaceArr[Mesh.ArrayType.VERTEX.ordinal] = vertexBuffer
            surfaceArr[Mesh.ArrayType.INDEX.ordinal] = indexBuffer

            // Use the importer to create a LOD to simplify our mesh for us
            // Because god knows I ain't doin that myself
            val im = ImporterMesh().apply { addSurface(Mesh.PrimitiveType.TRIANGLES, surfaceArr) }
            im.generateLods(20.0F, 0.0F, variantArrayOf())
            val lodIdxBuffer = im.getSurfaceLodIndices(0, im.getSurfaceLodCount(0) - 1)

            // The LOD is just an index buffer using the same vtx data,
            // so we just pass in the same vertex buffer with the new index buffer
            val lodArr = VariantArray<Any?>()
            lodArr.resize(Mesh.ArrayType.MAX.ordinal)
            lodArr[Mesh.ArrayType.VERTEX.ordinal] = vertexBuffer
            lodArr[Mesh.ArrayType.INDEX.ordinal] = lodIdxBuffer

            return ArrayMesh().apply { addSurfaceFromArrays(Mesh.PrimitiveType.TRIANGLES, lodArr) }
        }

        fun createFromMesh(mesh: ArrayMesh): SimpleNavGraph {
            /*
            1. collect neighbors for each face by getting edges, and then getting edge polygons that aren't that polygon
            2. Collect barycenters for each polygon
            3. Create connections between neighboring polygons
            */

            val meshTool = MeshDataTool()
            meshTool.createFromSurface(mesh, 0)

            // Maps to polygon index
            val polyRange = 0..meshTool.getFaceCount()
            // Calculate barycenters of each polygon
            val barycenters = polyRange.map { i ->
                val a = meshTool.getVertex(meshTool.getFaceVertex(i, 0))
                val b = meshTool.getVertex(meshTool.getFaceVertex(i, 1))
                val c = meshTool.getVertex(meshTool.getFaceVertex(i, 2))

                (a + b + c) / 3.0
            }

            // Collect a list of neighbors for each polygon
            val neighbors = polyRange.map { i ->
                val eA = meshTool.getFaceEdge(i, 0)
                val eB = meshTool.getFaceEdge(i, 1)
                val eC = meshTool.getFaceEdge(i, 2)

                arrayOf(
                    meshTool.getFaceEdge(i, 0),
                    meshTool.getFaceEdge(i, 1),
                    meshTool.getFaceEdge(i, 2)
                ) // Collect all 3 edges for this poly
                    .flatMap { e -> meshTool.getEdgeFaces(e) } // Get polys connected to those edges
                    .filterNot { n -> n == i } // Get rid of all the ones that are this polygon
            }


            // Add bidirectional connections to a set, so each is unique
            val connectedSet: MutableSet<UnorderedPair<Int, Int>> = mutableSetOf()
            neighbors.forEachIndexed { poly, nbrs ->
                nbrs.forEach { nbr -> connectedSet.add(UnorderedPair(poly, nbr)) }
            }

            // Flatten to indices, return navgraph
            return SimpleNavGraph().apply {
                this.points = PackedVector3Array(barycenters)
                this.connections = PackedInt32Array(connectedSet.flatMap { p -> listOf(p.first, p.second) })
            }
        }

        fun createFromNavMesh(nav: NavigationMesh): SimpleNavGraph = createFromMesh(createMeshFromNavmesh(nav))
    }
}
