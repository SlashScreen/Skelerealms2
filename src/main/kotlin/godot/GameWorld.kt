package godot

import godot.annotation.Emit
import godot.annotation.Register
import godot.annotation.Script
import godot.annotation.Visible
import godot.api.Node
import godot.core.Dictionary
import godot.core.StringName
import godot.core.signal1
import godot.entity.Entity
import godot.entity.EntityInstance
import godot.global.GD

@Script
class GameWorld : Node() {
    var room: StringName = StringName("")
    @Visible
    var currentRoom: StringName
        get() = room
        set(value) {
            room = value
            roomChanged.emit(room)
        }

    @Emit
    val roomChanged by signal1<StringName>()
    val entities: HashMap<StringName, Entity> = HashMap()

    @Register
    public fun addEntityFromInstance(instance: EntityInstance, userdata: Dictionary<StringName, Any?> = Dictionary()) {
        val n = instance.constructEntity(userdata)
        if (entities.containsKey(n.name)) {
            GD.printErr("Entity ${n.name} already exists")
            n.queueFree()
            return
        }
        entities[n.name] = n
    }

    @Register
    public fun getEntity(entityName: StringName): Entity? = entities[entityName]

    @Register
    public fun entityExists(entityName: StringName): Boolean = entities.containsKey(entityName)
}
