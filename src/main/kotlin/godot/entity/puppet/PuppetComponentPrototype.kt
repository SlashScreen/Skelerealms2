package godot.entity.puppet

import godot.annotation.Export
import godot.annotation.Script
import godot.api.PackedScene
import godot.core.Dictionary
import godot.core.StringName
import godot.entity.EntityComponent
import godot.entity.EntityComponentPrototype

@Script
class PuppetComponentPrototype : EntityComponentPrototype() {
    @Export
    var puppetScene: PackedScene? = null

    override fun constructComponent(userdata: Dictionary<StringName, Any?>): EntityComponent =
        PuppetComponent()
}
