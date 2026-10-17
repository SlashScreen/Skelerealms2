package godot.entity.inventory

import godot.annotation.Emit
import godot.annotation.Register
import godot.annotation.Script
import godot.annotation.Visible
import godot.core.*
import godot.entity.EntityComponent
import godot.entity.item.ItemComponent
import godot.loot.LootTree

@Script
class InventoryComponent : EntityComponent() {
    enum class InventoryChangeState { Add, Remove }

    /** Item ID that was added/removed, and whether it was added or removed */
    @Emit
    val inventoryChanged by signal2<StringName, InventoryChangeState>()

    /** Item ID that was equipped/unequipped, and whether it was added or removed */
    @Emit
    val equipmentChanged by signal2<StringName, InventoryChangeState>()

    /** Currency that has changed and the amount (negative if removed) */
    @Emit
    val currencyChanged by signal2<StringName, Int>()

    val inventory: MutableSet<StringName> = mutableSetOf()
    val currencies: HashMap<StringName, Int> = HashMap()
    val equipment: HashMap<StringName, StringName?> = HashMap()

    private val lootTree: LootTree?
        get() = instance?.let { (it as InventoryComponentPrototype).lootTree }

    @Visible
    var maxCarryWeight: Float
        get() = internalMaxCarryWeight ?: instance?.let { (it as InventoryComponentPrototype).maxCarryWeight } ?: 100F
        set(value) {
            internalMaxCarryWeight = value
        }
    private var internalMaxCarryWeight: Float? = null

    @Visible
    val currentCarryWeight: Float
        get() = inventory.fold(0F) { acc, id ->
            val en = entity?.gameWorld?.getEntity(id) ?: return acc
            val ic = en.getComponent<ItemComponent>() ?: return acc
            acc + (if (ic.questItem) 0F else ic.weight) // Quest item checks, quest items are weightless
        }

    fun generate() {
        // TODO: Generate, add items to the entity gameworld
    }

    // ITEMS/INVENTORY

    // This assumes canTakeItem is true. Don't call without checking
    private fun addItem(id: StringName) {
        entity?.let { e ->
            val en = e.gameWorld?.getEntity(id)!!
            val ic = en.getComponent<ItemComponent>()!!

            inventory.add(ic.name)
            ic.addToInventory(id)
            inventoryChanged.emit(id, InventoryChangeState.Add)
        }
    }

    private fun removeItem(id: StringName) {
        if (inventory.remove(id))
            inventoryChanged.emit(id, InventoryChangeState.Remove)
    }

    private fun canTakeItem(id: StringName): Boolean {
        return !inventory.contains(id) && entity?.let { e ->
            val en = e.gameWorld?.getEntity(id)!!
            val ic = en.getComponent<ItemComponent>()!!

            currentCarryWeight + ic.weight <= maxCarryWeight
        } ?: false
    }

    private fun canGiveItem(id: StringName): Boolean =
        inventory.contains(id) && entity?.let { e ->
            val en = e.gameWorld?.getEntity(id)!!
            val ic = en.getComponent<ItemComponent>()!!

            !ic.questItem
        } ?: false

    // CURRENCY

    @Register
    fun getCurrencyAmount(id: StringName): Int = currencies[id] ?: 0

    @Register
    fun setCurrencyAmount(id: StringName, amount: Int) {
        currencies[id] = amount
    }

    @Register
    fun getAllCurrencies(): VariantArray<StringName> = currencies.keys.toVariantArray()

    // EQUIPMENT

    @Register
    fun tryEquip(id: StringName, slot: StringName): Boolean {
        if (!equipment.containsKey(slot)) return false

        entity?.let { e ->
            val en = e.gameWorld?.getEntity(id)!!
            val ic = en.getComponent<ItemComponent>()!!

            if (ic.equipmentSlots.contains(slot)) {
                // if it's already equipped elsewhere, remove it from all slots
                equipment.onEach { (eSlot, item) -> if (item == id) equipment[eSlot] = null }
                equipment[slot] = id
                equipmentChanged.emit(id, InventoryChangeState.Remove)
                return true
            }
        }

        return false
    }

    @Register
    fun tryUnequip(id: StringName): Boolean {
        if (!equipment.containsValue(id)) return false

        equipment.onEach { (eSlot, item) -> if (item == id) equipment[eSlot] = null }
        equipmentChanged.emit(id, InventoryChangeState.Remove)

        return true
    }

    // TRANSACTIONS

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
                c.forEach { (cName, delta) ->
                    currencies[cName] = currencies.getOrDefault(cName, 0) + delta
                    currencyChanged.emit(cName, delta)
                }
            }
        } else { // Take from
            tx.items?.let { items ->
                items.forEach { item -> removeItem(item) }
            }
            tx.currencies?.let { c ->
                c.forEach { (cName, delta) ->
                    currencies[cName] = currencies.getOrDefault(cName, 0) - delta
                    currencyChanged.emit(cName, -delta)
                }
            }
        }
    }

    override fun getSaveData(): Dictionary<StringName, Any?> = dictionaryOf(
        "inventory".asCachedStringName() to inventory.toVariantArray(),
        "equipment".asCachedStringName() to equipment.toDictionary(),
        "currencies".asCachedStringName() to currencies.toDictionary(),
        "maxCarryWeight".asCachedStringName() to maxCarryWeight,
    )

    override fun loadFromData(data: Dictionary<StringName, Any?>) {
        inventory.clear()
        data["inventory".asCachedStringName()]?.let { i -> inventory.addAll((i as VariantArray<StringName>)) }
        equipment.clear()
        data["equipment".asCachedStringName()]?.let { e -> equipment.putAll((e as Dictionary<StringName, StringName?>)) }
        currencies.clear()
        data["currencies".asCachedStringName()]?.let { c -> currencies.putAll((c as Dictionary<StringName, Int>)) }
        internalMaxCarryWeight = null
        data["maxCarryWeight".asCachedStringName()]?.let { m -> internalMaxCarryWeight = (m as Float) }

    }

    override fun saveDataKey(): StringName = "inventoryComponent".asCachedStringName()

    // This is an attempt to use the type system to enforce checking
    // I don't know if it works.

    sealed interface AcceptedInventoryTransaction {
        fun getTransaction(): InventoryTransaction
    }

    internal class AcceptedTx(val tx: InventoryTransaction) : AcceptedInventoryTransaction {
        override fun getTransaction(): InventoryTransaction = tx
    }

    internal class TakeableItem(val id: StringName)
    internal class GiveableItem(val id: StringName)
}