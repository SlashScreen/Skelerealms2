package godot

import godot.annotation.Dir
import godot.annotation.Export
import godot.annotation.Script
import godot.api.Node
import godot.api.PackedScene
import godot.api.ResourceLoader
import godot.api.ResourceUID
import godot.core.StringName
import godot.core.asCachedStringName
import godot.extension.connectLambda
import godot.extension.connectMethod
import godot.extension.loadAs
import godot.global.GD

@Script
class RoomManager : Node() {
    @Dir
    var roomsPath: String = ""

    @Export
    var gameWorld: GameWorld? = null
    var room: Node? = null
    var rooms: HashMap<Int, Long> = hashMapOf()
    val regex = Regex(PATTERN)

    override fun _ready() {
        fillFromPath(roomsPath)
        GD.print(rooms)
        gameWorld!!.roomChanged.connectLambda { changeRoom(it) }
        GD.print("Room signal connected")
    }

    private fun fillFromPath(path: String) {
        val paths = ResourceLoader.listDirectory(path).map { p -> path + p }

        paths.forEach { path ->
            if (path.endsWith('/')) {
                fillFromPath(path)
            } else {
                val uid = ResourceLoader.getResourceUid(path)
                // If valid, try match, try load.
                if (uid != -1L) regex.matchEntire(path)?.let { matchResult ->
                    GD.print("Found room: ${matchResult.groupValues.getOrNull(1)}")
                    matchResult.groupValues.getOrNull(1)?.let { rooms[it.hashCode()] = uid }
                }
            }
        }
    }

    fun changeRoom(newRoom: StringName) {
        GD.print("Changing to $newRoom")

        room?.queueFree()
        // All this hash code shit because for some reason it wouldn't work eith string names.
        val ps = ResourceLoader.loadAs<PackedScene>(ResourceUID.getIdPath(rooms[newRoom.toString().hashCode()] ?: run {
            GD.pushError("Invalid room ID: $newRoom(${newRoom.toString().hashCode()}) - ${rooms.containsKey(newRoom.toString().hashCode())}")
            return
        })) ?: run {
            GD.pushError("Unable to load room: $newRoom")
            return
        }
        room = ps.instantiate()?.also { r -> addChild(r) } ?: run {
            GD.pushError("Unable to instantiate room: $newRoom")
            return
        }
    }

    companion object {
        const val PATTERN = """.*/(.*)\.(?:scn|tscn)"""
    }
}