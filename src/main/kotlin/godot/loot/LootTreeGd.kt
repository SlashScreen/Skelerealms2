package godot.loot

import godot.api.Resource
import godot.core.Dictionary
import godot.core.StringName
import godot.core.asCachedStringName

class LootTreeGd: LootTree() {
    var resource : Resource? = null

    override fun resolve(userdata: Dictionary<StringName, Any?>): LootTable {
        return resource?.call("resolve".asCachedStringName(), userdata) as LootTable ?: LootTable()
    }
}
