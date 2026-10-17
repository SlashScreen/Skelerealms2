package godot.entity.inventory

import godot.annotation.Script
import godot.annotation.Visible
import godot.core.Dictionary
import godot.core.StringName
import godot.entity.EntityComponent
import godot.entity.item.ItemComponent
import godot.loot.LootTree

@Script
class InventoryComponent : EntityComponent() {
    val inventory: MutableSet<StringName> = mutableSetOf()
    val currencies: HashMap<StringName, Int> = HashMap()
    val equipment: HashMap<StringName, StringName?> = HashMap()

    var lootTree: LootTree? = instance?.let { i -> (i as InventoryComponentPrototype).lootTree }

    @Visible
    var maxCarryWeight: Float = 100f // TODO: Allow this to change and stuff

    @Visible
    val currentCarryWeight: Float
        get() = inventory.fold(0F) { acc, id ->
            val en = entity?.gameWorld?.getEntity(id) ?: return acc
            val ic = en.getComponent<ItemComponent>() ?: return acc
            // TODO: Quest item checks
            acc + ic.weight
        }
    // TODO:
    // 2. equipment

    // This assumes canTakeItem is true. Don't call without checking
    private fun addItem(id: StringName) {
        entity?.let { e ->
            val en = e.gameWorld?.getEntity(id)!!
            val ic = en.getComponent<ItemComponent>()!!

            inventory.add(ic.name)
            ic.addToInventory(e.name)

            true
        }
    }

    private fun canTakeItem(id: StringName): Boolean {
        // TODO other checks like weight
        return !inventory.contains(id)
    }

    private fun canGiveItem(id: StringName): Boolean = inventory.contains(id) // TODO: Quest item checks

    fun canAcceptTransaction(tx: InventoryTransaction): AcceptedInventoryTransaction? {
        return if (tx.valid(entity?.gameWorld ?: return null) && if (tx.to == entity?.name) {
                // Make sure we can take the items
                tx.items?.let { items -> items.all { id -> canTakeItem(id) } } ?: true
            } else { // Take transaction from
                // Make sure we have the items and can give them
                (tx.items?.let { items -> items.all { id -> canGiveItem(id) } } ?: true)
                        // Make sure we have all the currencies necessary
                        && tx.currencies?.let { c ->
                    c.all { (name, delta) -> currencies.getOrDefault(name, 0) >= delta }
                } ?: true
            }
        ) AcceptedTx(tx)
        else null
    }

    // Transactions are supposed to be atomic to prevent item dupe glitches
    fun acceptTransaction(atx: AcceptedInventoryTransaction) {
        // Okay how do we make this atomic, how do we back out without causing too much drama
        val tx = atx.getTransaction()

        if (tx.to == entity?.name) { // Give to
            tx.items?.let { items ->
                items.forEach { item -> addItem(item) }
            }
            tx.currencies?.let { c ->
                c.forEach { (name, delta) -> currencies[name] = currencies.getOrDefault(name, 0) + delta }
            }
        } else { // Take from
            tx.items?.let { items ->
                items.forEach { item -> items.remove(item) }
            }
            tx.currencies?.let { c ->
                c.forEach { (name, delta) -> currencies[name] = currencies.getOrDefault(name, 0) - delta }
            }
        }
    }

    override fun getSaveData(): Dictionary<StringName, Any?>? {
        TODO("Not yet implemented")
    }

    override fun loadFromData(data: Dictionary<StringName, Any?>) {
        TODO("Not yet implemented")
    }

    override fun saveDataKey(): StringName {
        TODO("Not yet implemented")
    }

    // This is an attempt to use the type system to enforce checking
    // I don't know if it works.

    sealed interface AcceptedInventoryTransaction {
        fun getTransaction(): InventoryTransaction
    }

    internal class AcceptedTx(val tx: InventoryTransaction) : AcceptedInventoryTransaction {
        override fun getTransaction(): InventoryTransaction = tx
    }
}