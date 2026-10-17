package godot.loot

import godot.annotation.Register
import godot.annotation.Script
import godot.api.RefCounted
import godot.core.Dictionary
import godot.core.StringName
import godot.core.VariantArray
import godot.entity.item.ItemComponentPrototype

@Script
class LootTable() : RefCounted() {
    val prototypes: VariantArray<ItemComponentPrototype> = VariantArray()
    val currencies: Dictionary<StringName, Int> = Dictionary()
    val uniqueItems: VariantArray<StringName> = VariantArray()

    @Register
    fun merge(other: LootTable): LootTable {
        prototypes.appendArray(other.prototypes)
        uniqueItems.appendArray(other.uniqueItems)

        for ((c, v) in other.currencies) {
            currencies.get(c, null)?.let { i -> currencies[c] = v + i }
                ?: run { currencies[c] = v }
        }

        return this
    }

    companion object {
        @Register
        fun builder() = LootTableBuilder()
    }
}
