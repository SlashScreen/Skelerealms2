package godot.entity.attributes

import godot.annotation.Export
import godot.annotation.Script
import godot.core.Dictionary
import godot.core.StringName
import godot.core.VariantArray
import godot.entity.EntityComponent
import godot.entity.EntityComponentPrototype

@Script
class AttributesComponentPrototype : EntityComponentPrototype() {
    @Export
    var attributes: Dictionary<StringName, Float> = Dictionary()

    @Export
    var perks: VariantArray<StringName> = VariantArray()

    override fun constructComponent(userdata: Dictionary<StringName, Any?>): EntityComponent =
        AttributesComponent()
}
