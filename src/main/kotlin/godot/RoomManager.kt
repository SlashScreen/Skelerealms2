package godot

import godot.annotation.Export
import godot.annotation.Script
import godot.api.Node
import godot.api.PackedScene
import godot.api.Resource
import godot.core.StringName
import godot.extension.connectLambda
import godot.global.GD

@Script
class RoomManager : Node() {
    @Export
    var gameWorld: GameWorld? = null

    @Export
    var registry: Resource? = null
    var room: Node? = null

    private val runtimeRegistry = RuntimeRegistry<PackedScene>(registry ?: throw Error("Room manager needs registry"))

    override fun _ready() {
        gameWorld!!.roomChanged.connectLambda { changeRoom(it) }
    }

    fun changeRoom(newRoom: StringName) {
        room?.queueFree()

        val ps = runtimeRegistry.loadEntry(newRoom) ?: run {
            GD.pushError("No room with ID $newRoom")
            return
        }

        room = ps.instantiate()?.also { r -> addChild(r) } ?: run {
            GD.pushError("Unable to instantiate room: $newRoom")
            return
        }
    }
}
