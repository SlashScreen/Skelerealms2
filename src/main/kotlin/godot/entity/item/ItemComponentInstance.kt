package godot.entity.item

import godot.annotation.Export
import godot.annotation.Register
import godot.core.Dictionary
import godot.core.StringName
import godot.entity.EntityComponent
import godot.entity.EntityComponentInstance

class ItemComponentInstance : EntityComponentInstance() {
    @Export
    var prototype: ItemPrototype? = null

    override fun constructComponent(userdata: Dictionary<StringName, Any?>): EntityComponent {
        val res = this
        return ItemComponent().apply { buildFromInstance(res) }
    }
}
