package godot.navigation

import godot.RuntimeRegistry
import godot.annotation.Register
import godot.annotation.Script
import godot.api.AStar3D
import godot.api.Node
import godot.core.*
import godot.global.GD
import org.jgrapht.Graph
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultUndirectedWeightedGraph
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.nio.graph6.Graph6Sparse6Exporter
import org.jgrapht.nio.graph6.Graph6Sparse6Importer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/* TODO:
* Convert doors to using stringname keys because cyclic
*/

/** Master script for navigating the world between cells. */
@Script
class Navigator : Node() {
    /** A graph containing vertices for rooms, and edges for doors between the rooms */
    private val roomGraph: Graph<StringName, PathEdge> = DefaultUndirectedWeightedGraph(PathEdge::class.java)

    /** Navigation graphs used for beginning and end / same-room calculations. */
    private val navMaps = hashMapOf<StringName, AStar3D>()

    /** Path cache. */
    private val cache = hashMapOf<Pair<StringName, StringName>, List<PathStep>>()

    // TODO: project setting?
    private val doorRegistry = RuntimeRegistry<DoorInstance>(DOOR_PATH)
    private val graphRegistry = RuntimeRegistry<SimpleNavGraph>(NAV_PATH)

    override fun _enterTree() {
        instance = this
    }

    override fun _ready() {
        // Load all doors and group by room
        val doors = doorRegistry.loadAllBlocking()
            .values
            .groupBy { it.room }
        GD.print(doors)

        // Load graphs and grab room name from path
        val graphs = graphRegistry.loadAllBlocking()
        GD.print(graphs)

        // Load all found graphs
        graphs.forEach { (room, graph) ->
            loadRoom(room, graph, doors[room] ?: listOf())
        }
        GD.print(roomGraph.toString())
    }

    /** Invalidate the internal path cache. */
    fun invalidateCache() {
        cache.clear()
    }

    /** Find a path between two points, given as a series of [PathStep]s. Null if no path found. */
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

        val pathGen =
            DijkstraShortestPath(roomGraph) // Dijkstra since we don't super have a good distance thing? Unless I can finagle something with edges
        val path = pathGen.getPath(a.first, b.first) ?: run {
            GD.print("Could not find path between rooms ${a.first} and ${b.first}")
            return null
        }

        // First

        val verts = path.vertexList
        val edges = path.edgeList
        val steps = mutableListOf<PathStep>()

        run {
            // Add path to door, then switch to next room
            var first = edges.first()!!

            val ptA = astar.getClosestPoint(a.second).also { GD.print(it) }
            val ptB = astar.getClosestPoint(first.endPortalIn).also { GD.print(it) }
            // TODO: What if they are the same point?
            val path = astar.getPointPath(ptA, ptB) // Why doesn't this work?

            if (path.isEmpty()) {
                GD.print("Cannot find path from point to first door. Cancelling path.")
                return null
            }
            path.append(first.endPortalIn)
            steps.add(PointsPathStep(path))
            steps.add(AbsoluteStep(verts[1], first.endPortalOut))
        }

        // Middle

        if (verts.size > 2) {
            // If there is a cache entry, get it
            cache[Pair(a.first, b.first)]?.let { steps.addAll(it) }
            // Otherwise:
            // Add path to next door, move to next room
            // For all in middle
                ?: (1 until edges.size - 1).forEach { i ->
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

            if (path.isEmpty()) {
                GD.print("Cannot find path from last door to point. Cancelling path.")
                return null
            }
            steps.add(PointsPathStep(path))
        }

        if (steps.size > 2) cache[Pair(a.first, b.first)] = steps.slice(1 until steps.size)
        GD.print("Calculated path: $steps")

        return steps
    }

    /**
     * [findPath] for GDScript. Rendered into a list of dicts, each representing a step.
     * [AbsoluteStep] is rendered as:
     * ```
     * {
     *  &"type": &"absolute",
     *  &"room": StringName,
     *  &"pos": Vector3,
     * }
     * ```
     * [PointsPathStep] is rendered as:
     * ```
     * {
     *  &"type": &"points",
     *  &"path": PackedVector3Array,
     * }
     * ```
     * */
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

    /** Load a [SimpleNavGraph] as an [godot.api.AStar3D] graph. */
    @Register
    fun loadRoomAStar(roomName: StringName, mesh: SimpleNavGraph) {
        navMaps[roomName] = mesh.intoAStar()
    }

    // TODO: support chunks?
    /** Load a room, providing a nav graph and a list of doors in that room. */
    fun loadRoom(roomName: StringName, mesh: SimpleNavGraph, doors: List<DoorInstance>) {
        GD.print("Loading room graph $roomName...")
        val astar = mesh.intoAStar()
        navMaps[roomName] = astar

        // Get room connections
        // 1. Finds one door and another
        // 2. Tries to connect
        // 3. If they connect, this room can connect to the other, so add that as the edge with length as weight

        if (doors.size == 1) {
            val a = doors.first()
            val roomA = a.room
            // TODO: The cache should cover this but maybe dont load it again
            // We could just pass in All doors and use that since we have it by this point.
            val otherSide = doorRegistry.loadEntry(a.otherSide) ?: run {
                GD.print("Door has no other side. Skipping...")
                return
            } // Skip if door has no other side
            val roomB = otherSide.room

            roomGraph.addVertex(roomA)
            roomGraph.addVertex(roomB)
            roomGraph.addEdge(
                roomA, roomB, PathEdge(
                    PackedVector3Array(),
                    a.position,
                    otherSide.position, // If we've gotten here we know this isn't null
                    1.0
                )
            )
        } else
            for (a in doors) {
                for (b in doors) {
                    if (a == b) continue // Skip if same door

                    val roomA = a.room
                    // TODO: The cache should cover this but maybe dont load it again
                    // We could just pass in All doors and use that since we have it by this point.
                    val otherSide = doorRegistry.loadEntry(b.otherSide) ?: run {
                        GD.print("Door has no other side. Skipping...")
                        continue
                    } // Skip if door has no other side
                    val roomB = otherSide.room

                    // Calculate path

                    val pointA = astar.getClosestPoint(a.position)
                    val pointB = astar.getClosestPoint(b.position)

                    val path = astar.getPointPath(pointA, pointB)
                    if (path.isEmpty()) run {
                        GD.print("No path could be baked. Skipping...")
                        continue
                    } // Skip if cannot find path

                    // Get length (squared) of path. Accurate length doesn't matter for path calculations.
                    var lastVertex = path.first()
                    val pathCost = path.fold(0.0) { accum, next ->
                        (accum + lastVertex.distanceSquaredTo(next)).also {
                            lastVertex = next
                        }
                    }

                    // Add connection
                    GD.print("Connecting and adding rooms $roomA and $roomB")
                    roomGraph.addVertex(roomA)
                    roomGraph.addVertex(roomB)
                    roomGraph.addEdge(
                        roomA, roomB, PathEdge(
                            path,
                            b.position,
                            otherSide.position, // If we've gotten here we know this isn't null
                            pathCost
                        )
                    )
                }
            }
    }

    /** Serialize the graph for disk cache. */
    @Register
    fun serializeGraph(): PackedByteArray {
        val g6 = Graph6Sparse6Exporter<StringName, PathEdge>(Graph6Sparse6Exporter.Format.SPARSE6)
        val writer = ByteArrayOutputStream()

        g6.exportGraph(roomGraph, writer)

        return writer.toByteArray().toPackedByteArray()
    }

    /** Deserialize the graph from disk cache. */
    @Register
    fun deserializeGraph(buffer: PackedByteArray) {
        val g6 = Graph6Sparse6Importer<StringName, PathEdge>()
        val reader = ByteArrayInputStream(buffer.toByteArray())

        g6.importGraph(roomGraph, reader)
    }

    /** Custom edge class holding path information. */
    class PathEdge(
        val path: PackedVector3Array,
        val endPortalIn: Vector3,
        val endPortalOut: Vector3,
        val edgeWeight: Double
    ) : DefaultWeightedEdge() {
        override fun getWeight(): Double = edgeWeight
    }

    /** A step in a multi-room path. */
    sealed class PathStep

    /** Step to teleport to a room and point - going through a door. */
    class AbsoluteStep(val room: StringName, val pos: Vector3) : PathStep()

    /** A series of paths inside a room to follow. */
    class PointsPathStep(val path: PackedVector3Array) : PathStep()

    companion object {
        // TODO: Make this a setting
        private const val DOOR_PATH = "res://doors.tres"
        private const val NAV_PATH = "res://navgraphs.tres"

        /** Singleton navigator instance. */
        lateinit var instance: Navigator
    }
}
