package godot.navigation

import godot.annotation.Script
import godot.core.Dictionary
import godot.core.StringName
import godot.entity.EntityComponent
import godot.entity.EntityComponentPrototype

@Script
class NavigatorComponentPrototype : EntityComponentPrototype() {
    override fun constructComponent(userdata: Dictionary<StringName, Any?>): EntityComponent = NavigatorComponent()
}
