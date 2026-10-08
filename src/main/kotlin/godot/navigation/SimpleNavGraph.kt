package godot.navigation

import godot.annotation.Export
import godot.annotation.Script
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
        fun createFromNavMesh(nav: NavigationMesh): SimpleNavGraph {
            // Calculates points from the centers of polygons and connections by connecting polygons that share at least one vertex

            val polysTouchingPoint: HashMap<Int, MutableSet<Int>> = HashMap()
            val barycenters: Array<Vector3> = Array(nav.getPolygonCount()) { Vector3() }

            val vtxs = nav.getVertices()
            for (i in 0 until nav.getPolygonCount()) {
                val indices = nav.getPolygon(i)
                barycenters[i] = indices.map { vtxs[it] }
                    .reduce { vector3, acc -> acc + vector3 } / nav.verticesPerPolygon // calculate barycenter
                indices.forEach { idx ->
                    polysTouchingPoint.getOrDefault(idx, mutableSetOf()).add(i)
                } // add this polygon to the list of polygons touching a vertex
            }

            // Does this do what I want?
            val connections: MutableSet<UnorderedPair<Int, Int>> = mutableSetOf()
            for ((_, polys) in polysTouchingPoint) {
                polys.forEach { a ->
                    polys.forEach { b ->
                        if (a != b) {
                            val pair = UnorderedPair(a, b)
                            connections.add(pair)
                        }
                    }
                }
            }

            // Flatten
            val cns = connections.fold(mutableListOf<Int>()) { acc, pair ->
                acc.apply {
                    add(pair.first)
                    add(pair.second)
                }
            }

            // TODO:
            // Doors
            // Weights - calculate paths between doors

            return SimpleNavGraph().apply {
                this.points = PackedVector3Array(barycenters)
                this.connections = PackedInt32Array(cns)
            }
        }
    }
}
