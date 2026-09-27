package godot.loot

import godot.annotation.Export
import godot.annotation.Script
import godot.core.Dictionary
import godot.core.StringName
import godot.core.VariantArray

@Script
class LootLeaf : LootTree() {
    @Export
    var prototypes: VariantArray<Int> = VariantArray()
    @Export
    var currencies: Dictionary<StringName, Int> = Dictionary()
    @Export
    var uniqueItems: VariantArray<StringName> = VariantArray()

    override fun resolve(userdata: Dictionary<StringName, Any?>): LootTable =
        LootTable.builder().withPrototypes(prototypes).withCurrencies(currencies).withUniqueItems(uniqueItems).build()
}