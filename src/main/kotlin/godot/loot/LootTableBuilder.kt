package godot.loot

import godot.annotation.Register
import godot.annotation.Script
import godot.api.RefCounted
import godot.core.Dictionary
import godot.core.StringName
import godot.core.VariantArray
import godot.entity.item.ItemComponentPrototype

@Script
class LootTableBuilder : RefCounted() {
    private val lt = LootTable()

    @Register
    fun withPrototypes(prototypes: VariantArray<ItemComponentPrototype>): LootTableBuilder {
        lt.prototypes.addAll(prototypes)
        return this
    }

    @Register
    fun withUniqueItems(uniqueItems: VariantArray<StringName>): LootTableBuilder {
        lt.uniqueItems.addAll(uniqueItems)
        return this
    }

    @Register
    fun withCurrencies(currencies: Dictionary<StringName, Int>): LootTableBuilder {
        lt.currencies.merge(currencies)
        return this
    }

    @Register
    fun build(): LootTable = lt
}
