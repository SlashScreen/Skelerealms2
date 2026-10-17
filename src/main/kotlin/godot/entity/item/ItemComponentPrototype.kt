package godot.entity.item

import godot.annotation.Export
import godot.annotation.Script
import godot.api.PackedScene
import godot.core.Dictionary
import godot.core.StringName
import godot.entity.EntityComponent
import godot.entity.EntityComponentPrototype

@Script
class ItemComponentPrototype : EntityComponentPrototype() {
    @Export
    var model: PackedScene? = null
    @Export
    var baseCurrency: StringName = StringName("")
    @Export
    var baseCost: Int = 0
    @Export
    var weight: Float = 1f

    override fun constructComponent(userdata: Dictionary<StringName, Any?>): EntityComponent = ItemComponent()
}
