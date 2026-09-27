package godot.loot

import godot.annotation.Register
import godot.annotation.Script
import godot.api.Resource
import godot.core.Dictionary
import godot.core.StringName

@Script
abstract class LootTree : Resource() {
    @Register
    abstract fun resolve(userdata: Dictionary<StringName, Any?>): LootTable
}
