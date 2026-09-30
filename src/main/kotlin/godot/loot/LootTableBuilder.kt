package godot.loot

import godot.annotation.Register
import godot.annotation.Script
import godot.api.RefCounted
import godot.core.Dictionary
import godot.core.StringName
import godot.core.VariantArray

@Script
class LootTableBuilder : RefCounted() {
    val lt = LootTable()

    @Register
    public fun withPrototypes(prototypes: VariantArray<Int>): LootTableBuilder {
        lt.prototypes.addAll(prototypes)
        return this
    }

    @Register
    public fun withUniqueItems(uniqueItems: VariantArray<StringName>): LootTableBuilder {
        lt.uniqueItems.addAll(uniqueItems)
        return this
    }

    @Register
    public fun withCurrencies(currencies: Dictionary<StringName, Int>): LootTableBuilder {
        lt.currencies.merge(currencies)
        return this
    }

    @Register
    public fun build(): LootTable = lt
}
