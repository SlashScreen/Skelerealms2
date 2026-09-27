package godot.entity.item

import godot.annotation.Export
import godot.annotation.Script
import godot.api.PackedScene
import godot.api.Resource
import godot.core.StringName

@Script
class ItemPrototype : Resource() {
    @Export
    var model: PackedScene? = null
    @Export
    var baseCurrency: StringName = StringName("")
    @Export
    var baseCost: Int = 0
    @Export
    var weight: Float = 1f
}
