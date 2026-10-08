package godot.navigation

import godot.api.AStar3D
import godot.core.StringName
import godot.core.Vector3
import godot.global.GD
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DefaultUndirectedWeightedGraph

class Navigator {
    val roomGraph: Graph<StringName, DefaultEdge> = DefaultUndirectedWeightedGraph(DefaultEdge::class.java)
    val navMaps = hashMapOf<StringName, AStar3D>()
    val cache = hashMapOf<Pair<StringName, StringName>, List<StringName>>()
    val roomConnections =
        hashMapOf<StringName, HashMap<StringName, Triple<Vector3, Vector3, Boolean>>>() // first vector is portal location in first room, second is position in second

    fun invalidateCache() {
        cache.clear()
    }

    // Does this make GC crap
    private tailrec fun bfs(
        todo: List<StringName>,
        seen: Set<StringName> = setOf(),
        order: List<StringName> = listOf(),
    ): List<StringName> {
        if (todo.isEmpty()) return order

        val first = todo.first()
        val tail = todo.drop(1)

        return if (first in seen) bfs(tail, seen, order)
        else {
            val connections = roomConnections[first]?.filterNot { it.key in seen || it.value.third }
                ?.map { it.key } // Get rooms that aren't in seen or locked
            bfs(connections?.let { it + tail } ?: tail, seen + first, order + first)
        }
    }

    fun getRoomPath(a: StringName, b: StringName): List<StringName>? =
        if (a == b)
            null
        else
            cache[Pair(a, b)] ?: bfs(roomConnections[a]?.keys?.toList() ?: return null)
                .also { cache[Pair(a, b)] = it }

    // TODO: Figure out a more optimal way to do this
    fun getPathBetween(a: Pair<StringName, Vector3>, b: Pair<StringName, Vector3>): List<PathStep> {
        val roomPath = getRoomPath(a.first, b.first) ?: return listOf() // TODO: make point path instead of empty
        val res = mutableListOf<PathStep>()

        /*
        * 1. Move two at a time
        * 2. Push path from last position to roomConnections 0
        * 3. Push portal
        * */

        // Add start
        res.add(AbsoluteStep(a.first, a.second))

        var lastRoom = a.first
        var lastPos = a.second

        for (room in roomPath.slice(1..<roomPath.lastIndex)) {
            val info = roomConnections[lastRoom]?.get(room)
                ?: return listOf<PathStep>().also { GD.pushError("Something has gone awry while creating a path - no room connection found") }

            // Calculate path to next portal
            val astar = navMaps[lastRoom]
                ?: return listOf<PathStep>().also { GD.pushError("Something has gone awry while creating a path - no navmesh found") }
            val start = astar.getClosestPoint(lastPos)
            val end = astar.getClosestPoint(info.first)

            val pointPath = navMaps[lastRoom]?.getPointPath(start, end, true)?.toList()
                ?: return listOf<PathStep>().also { GD.pushError("Something has gone awry while creating a path - path not computed") }
            res.add(PointsPathStep(pointPath))

            // Teleport into new room
            res.add(AbsoluteStep(room, info.second))

            lastRoom = room
            lastPos = info.second
        }

        // Add end
        res.add(AbsoluteStep(b.first, b.second))

        return res
    }

    // TODO: Portal can be replaced with an absolute
    abstract class PathStep
    class AbsoluteStep(val room: StringName, val pos: Vector3) : PathStep() // Teleport to a point
    class PointsPathStep(val path: List<Vector3>) : PathStep() // Move along a path
}
