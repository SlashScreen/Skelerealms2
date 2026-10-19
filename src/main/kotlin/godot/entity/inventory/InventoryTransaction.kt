package godot.entity.inventory

import godot.GameWorld
import godot.annotation.Register
import godot.annotation.Script
import godot.api.RefCounted
import godot.core.Dictionary
import godot.core.StringName
import godot.core.VariantArray
import godot.entity.EntityComponent
import godot.entity.item.ItemComponent

@Script
class InventoryTransaction : RefCounted() {
    var from: StringName? = null
    var to: StringName? = null

    var items: VariantArray<StringName>? = null
    var currencies: Dictionary<StringName, Int>? = null

    fun valid(gwHandle: GameWorld): Boolean {
        return from?.let { f ->
            to != null && f != to // TODO: Check if all are items
        } ?: items?.all { item ->
            // Check if all are items and hare not in an inventory
            val e = gwHandle.getEntity(item) ?: return false
            val ic = e.getComponent<ItemComponent>() ?: return false

            !ic.inInventory
        } ?: true
    }

    @Register
    fun withFrom(from: StringName): InventoryTransaction {
        this.from = from
        return this
    }

    @Register
    fun withTo(to: StringName): InventoryTransaction {
        this.to = to
        return this
    }

    @Register
    fun withItems(itemsList: VariantArray<StringName>): InventoryTransaction {
        items = itemsList
        return this
    }

    @Register
    fun withCurrencies(currenciesList: Dictionary<StringName, Int>): InventoryTransaction {
        currencies = currenciesList
        return this
    }
}
