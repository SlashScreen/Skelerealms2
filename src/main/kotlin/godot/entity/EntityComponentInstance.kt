package godot.entity

import godot.annotation.Register
import godot.annotation.Script
import godot.api.Resource
import godot.core.Dictionary
import godot.core.StringName

@Script
abstract class EntityComponentInstance : Resource() {
    @Register
    abstract fun constructComponent(userdata: Dictionary<StringName, Any?>): EntityComponent
}
