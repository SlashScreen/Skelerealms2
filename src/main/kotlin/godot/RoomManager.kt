package godot

import godot.annotation.Export
import godot.annotation.HintString
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

    @HintString("RuntimeRegistry")
    var registry: Resource? = null
    var room: Node? = null

    private val runtimeRegistry: RuntimeRegistry<PackedScene>
        get() {
            if (!this::lateRuntimeRegistry.isInitialized) {
                lateRuntimeRegistry = RuntimeRegistry<PackedScene>(
                    registry ?: throw Error("RoomManager needs a backing registry")
                )
            }
            return lateRuntimeRegistry
        }
    private lateinit var lateRuntimeRegistry: RuntimeRegistry<PackedScene>

    override fun _ready() {
        gameWorld!!.roomChanged.connectLambda { changeRoom(it) }
    }

    fun changeRoom(newRoom: StringName) {
        room?.queueFree()

        val ps = runtimeRegistry.loadEntry(newRoom) ?: run {
            GD.pushError("No room with ID $newRoom (${runtimeRegistry.names})")
            return
        }

        room = ps.instantiate()?.also { r -> addChild(r) } ?: run {
            GD.pushError("Unable to instantiate room: $newRoom")
            return
        }
    }
}
