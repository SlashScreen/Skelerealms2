package godot.loot

import godot.annotation.Export
import godot.annotation.Script
import godot.core.Dictionary
import godot.core.StringName
import godot.core.VariantArray

@Script
class LootRoll : LootTree() {
    @Export
    var lootNodes: VariantArray<LootTree> = VariantArray()

    override fun resolve(userdata: Dictionary<StringName, Any?>): LootTable =
        lootNodes.pickRandom()?.resolve(userdata) ?: LootTable()
}
