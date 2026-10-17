package godot.entity.attributes

import godot.annotation.Export
import godot.annotation.Script
import godot.core.Dictionary
import godot.core.StringName
import godot.entity.EntityComponent
import godot.entity.EntityComponentInstance

@Script
class AttributesComponentInstance : EntityComponentInstance() {
    @Export
    var attributes: Dictionary<StringName, Float> = Dictionary()

    override fun constructComponent(userdata: Dictionary<StringName, Any?>): EntityComponent =
        AttributesComponent()
}
