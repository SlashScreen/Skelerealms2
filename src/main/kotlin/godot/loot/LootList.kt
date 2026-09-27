package godot.loot

import godot.annotation.Export
import godot.annotation.Script
import godot.core.Dictionary
import godot.core.StringName
import godot.core.VariantArray

@Script
class LootList : LootTree() {
    @Export
    var items: VariantArray<LootTree> = VariantArray()

    override fun resolve(userdata: Dictionary<StringName, Any?>): LootTable =
        items.fold(LootTable()) { acc, tree -> acc.merge(tree.resolve(userdata)) }
}
