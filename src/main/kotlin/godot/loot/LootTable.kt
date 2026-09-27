package godot.loot

import godot.annotation.Register
import godot.annotation.Script
import godot.api.RefCounted
import godot.core.Dictionary
import godot.core.StringName
import godot.core.VariantArray

@Script
class LootTable() : RefCounted() {
    val prototypes: VariantArray<Int> = VariantArray()
    val currencies: Dictionary<StringName, Int> = Dictionary()
    val uniqueItems: VariantArray<StringName> = VariantArray()

    @Register
    public fun merge(other: LootTable): LootTable {
        prototypes.appendArray(other.prototypes)
        uniqueItems.appendArray(other.uniqueItems)

        for ((c, v) in other.currencies) {
            currencies.get(c, null)?.let { i -> currencies[c] = v + i }
                ?: run { currencies[c] = v }
        }

        return this
    }

    @Script
    class Builder : RefCounted() {
        val lt = LootTable()

        @Register
        public fun withPrototypes(prototypes: VariantArray<Int>): Builder {
            lt.prototypes.addAll(prototypes)
            return this
        }

        @Register
        public fun withUniqueItems(uniqueItems: VariantArray<StringName>): Builder {
            lt.uniqueItems.addAll(uniqueItems)
            return this
        }

        @Register
        public fun withCurrencies(currencies: Dictionary<StringName, Int>): Builder {
            lt.currencies.merge(currencies)
            return this
        }

        @Register
        public fun build(): LootTable = lt
    }

    companion object {
        @Register
        public fun builder() = Builder()
    }
}
