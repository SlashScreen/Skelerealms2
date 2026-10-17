package godot.entity.faction

import godot.annotation.Export
import godot.annotation.Script
import godot.core.Dictionary
import godot.core.StringName
import godot.entity.EntityComponent
import godot.entity.EntityComponentPrototype

@Script
class FactionsComponentPrototype : EntityComponentPrototype() {
    @Export
    var factions: Dictionary<StringName, Long> = Dictionary()

    override fun constructComponent(userdata: Dictionary<StringName, Any?>): EntityComponent = FactionsComponent()
}