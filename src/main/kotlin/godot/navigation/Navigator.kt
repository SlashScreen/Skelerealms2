package godot.navigation

import godot.annotation.Register
import godot.annotation.Script
import godot.api.AStar3D
import godot.api.Node
import godot.core.*
import org.jgrapht.Graph
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultUndirectedWeightedGraph
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.nio.graph6.Graph6Sparse6Exporter
import org.jgrapht.nio.graph6.Graph6Sparse6Importer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@Script
class Navigator : Node() {
    val roomGraph: Graph<StringName, PathEdge> = DefaultUndirectedWeightedGraph(PathEdge::class.java)
    val navMaps = hashMapOf<StringName, AStar3D>()
    val cache = hashMapOf<Pair<StringName, StringName>, List<PathStep>>()

    override fun _enterTree() {
        instance = this
    }

    fun invalidateCache() {
        cache.clear()
    }

    fun findPath(a: Pair<StringName, Vector3>, b: Pair<StringName, Vector3>): List<PathStep>? {
        val astar = navMaps[a.first] ?: return null

        if (a.first == b.first) {
            // Special case: same world, calculate a path
            val ptA = astar.getClosestPoint(a.second)
            val ptB = astar.getClosestPoint(b.second)

            val path = astar.getPointPath(ptA, ptB, true) // Allow partial paths

            return if (path.isEmpty()) null
            else listOf(PointsPathStep(path))
        }
        cache[Pair(a.first, b.first)]?.let { return it } // If there is a cache entry, get it

        val pathGen =
            DijkstraShortestPath(roomGraph) // Dijkstra since we don't super have a good distance thing? Unless I can finagle something with edges
        val path = pathGen.getPath(a.first, b.first) ?: return null

        // First

        val verts = path.vertexList
        val edges = path.edgeList
        val steps = mutableListOf<PathStep>()

        run {
            // Add path to door, then switch to next room
            var first = edges.first()!!

            val ptA = astar.getClosestPoint(a.second)
            val ptB = astar.getClosestPoint(first.endPortalIn)
            val path = astar.getPointPath(ptA, ptB)

            if (path.isEmpty()) return null
            steps.add(PointsPathStep(path))
            steps.add(AbsoluteStep(verts[1], first.endPortalOut))
        }

        // Middle

        if (verts.size > 2) {
            // Add path to next door, move to next room
            // For all in middle
            (1 until edges.size - 1).forEach { i ->
                val edge = edges[i]!! // if this is null we did an oopsie

                steps.add(PointsPathStep(edge.path))
                steps.add(AbsoluteStep(verts[i + 1], edge.endPortalOut))
            }
        }

        // Last

        run {
            // Add path from door to final point
            var last = edges.last()!!

            val ptA = astar.getClosestPoint(last.endPortalOut)
            val ptB = astar.getClosestPoint(b.second)
            val path = astar.getPointPath(ptA, ptB, true) // Allow incomplete path here

            if (path.isEmpty()) return null
            steps.add(PointsPathStep(path))
        }

        cache[Pair(a.first, b.first)] = steps

        return steps
    }

    @Register
    fun findPathBetweenRooms(
        roomA: StringName,
        posA: Vector3,
        roomB: StringName,
        posB: Vector3
    ): VariantArray<Dictionary<StringName, Any?>> {
        val steps = findPath(Pair(roomA, posA), Pair(roomB, posB)) ?: return variantArrayOf()

        return steps.map<PathStep, Dictionary<StringName, Any?>> {
            when (it) {
                is AbsoluteStep -> {
                    dictionaryOf(
                        "type".asCachedStringName() to "absolute".asCachedStringName(),
                        "room".asCachedStringName() to it.room,
                        "pos".asCachedStringName() to it.pos,
                    )
                }

                is PointsPathStep -> {
                    dictionaryOf(
                        "type".asCachedStringName() to "points".asCachedStringName(),
                        "path".asCachedStringName() to it.path,
                    )
                }
            }
        }.toVariantArray()
    }

    @Register
    fun loadRoomAStar(roomName: StringName, mesh: SimpleNavGraph) {
        navMaps[roomName] = mesh.intoAStar()
    }

    // TODO: support chunks?
    fun loadRoom(roomName: StringName, mesh: SimpleNavGraph, doors: List<DoorInstance>) {
        val astar = mesh.intoAStar()
        navMaps[roomName] = astar

        // Get room connections
        // 1. Finds one door and another
        // 2. Tries to connect
        // 3. If they connect, this room can connect to the other, so add that as the edge with length as weight
        for (a in doors) {
            for (b in doors) {
                if (a == b) continue // Skip if same door

                val roomA = a.room
                val roomB = b.otherSide?.room ?: continue // Skip if door has no other side

                // Calculate path

                val pointA = astar.getClosestPoint(a.position)
                val pointB = astar.getClosestPoint(b.position)

                val path = astar.getPointPath(pointA, pointB)
                if (path.isEmpty()) continue // Skip if cannot find path

                // Get length (squared) of path. Accurate length doesn't matter for path calculations.
                var lastVertex = path.first()
                val pathCost = path.fold(0.0) { accum, next ->
                    (accum + lastVertex.distanceSquaredTo(next)).also {
                        lastVertex = next
                    }
                }

                // Add connection
                roomGraph.addVertex(roomA)
                roomGraph.addVertex(roomB)
                roomGraph.addEdge(
                    roomA, roomB, PathEdge(
                        path,
                        b.position,
                        b.otherSide!!.position, // If we've gotten here we know this isn't null
                        pathCost
                    )
                )
            }
        }
    }

    @Register
    fun serializeGraph(): PackedByteArray {
        val g6 = Graph6Sparse6Exporter<StringName, PathEdge>(Graph6Sparse6Exporter.Format.SPARSE6)
        val writer = ByteArrayOutputStream()

        g6.exportGraph(roomGraph, writer)

        return writer.toByteArray().toPackedByteArray()
    }

    @Register
    fun deserializeGraph(buffer: PackedByteArray) {
        val g6 = Graph6Sparse6Importer<StringName, PathEdge>()
        val reader = ByteArrayInputStream(buffer.toByteArray())

        g6.importGraph(roomGraph, reader)
    }

    class PathEdge(
        val path: PackedVector3Array,
        val endPortalIn: Vector3,
        val endPortalOut: Vector3,
        val edgeWeight: Double
    ) : DefaultWeightedEdge() {
        override fun getWeight(): Double = edgeWeight
    }

    sealed class PathStep
    class AbsoluteStep(val room: StringName, val pos: Vector3) : PathStep() // Teleport to a point
    class PointsPathStep(val path: PackedVector3Array) : PathStep() // Move along a path

    companion object {
        var instance: Navigator? = null
    }
}
