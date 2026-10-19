package godot.entity.inventory

import godot.annotation.Emit
import godot.annotation.Register
import godot.annotation.Script
import godot.annotation.Visible
import godot.core.*
import godot.entity.EntityComponent
import godot.entity.item.ItemComponent
import godot.extension.connectLambda
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

    /** Inventory set. */
    val inventory: MutableSet<StringName> = mutableSetOf()
    /** Currency type -> amount. */
    val currencies: HashMap<StringName, Int> = HashMap()
    /** Equipment for this inventory. Editor tools should keep this limited to valid slots. */
    val equipment: HashMap<StringName, StringName?> = HashMap()

    /** Loot tree, used for generation. */
    private val lootTree: LootTree?
        get() = prototype?.let { (it as InventoryComponentPrototype).lootTree }

    /** The maximum carry weight for this inventory. */
    @Visible
    var maxCarryWeight: Float
        get() = internalMaxCarryWeight ?: prototype?.let { (it as InventoryComponentPrototype).maxCarryWeight } ?: 100F
        set(value) {
            internalMaxCarryWeight = value
        }
    /** Override max carry weight, otherise it defers to prototype. */
    private var internalMaxCarryWeight: Float? = null

    /** The current sum of all item weights. */
    @Visible
    val currentCarryWeight: Float
        get() = carryWeightCache ?: inventory.fold(0F) { acc, id ->
            val en = entity?.gameWorld?.getEntity(id) ?: return acc
            val ic = en.getComponent<ItemComponent>() ?: return acc
            acc + (if (ic.questItem) 0F else ic.weight) // Quest item checks, quest items are weightless
        }.also { carryWeightCache = it }
    /** Internal cache for carry weight to avoid calculating every access. */
    private var carryWeightCache: Float? = null

    fun generate() {
        // TODO: Generate, add items to the entity gameworld
    }

    // GODOT

    override fun _ready() {
        inventoryChanged.connectLambda { _, _ -> carryWeightCache = null  } // Reset carry weight cache every time it changes
    }

    // ITEMS/INVENTORY

    /** Add an item to the inventory. Assumes canTakeItem is true. */
    private fun addItem(id: StringName) {
        entity?.let { e ->
            val en = e.gameWorld?.getEntity(id)!!
            val ic = en.getComponent<ItemComponent>()!!

            inventory.add(ic.name)
            ic.addToInventory(id)
            inventoryChanged.emit(id, InventoryChangeState.Add)
        }
    }

    /** Remove an item from inventory. Assumes canGiveItem. */
    private fun removeItem(id: StringName) {
        if (inventory.remove(id))
            inventoryChanged.emit(id, InventoryChangeState.Remove)
    }

    /** Determine if this can take the item. */
    private fun canTakeItem(id: StringName): Boolean {
        return (!inventory.contains(id)) && entity?.let { e ->
            val en = e.gameWorld?.getEntity(id)!!
            val ic = en.getComponent<ItemComponent>()!!
            log("checking weight: ${currentCarryWeight + ic.weight} <= $maxCarryWeight")
            currentCarryWeight + ic.weight <= maxCarryWeight // TODO: Encumbrance?
        } ?: false
    }

    /** Determine if this item can be taken. */
    private fun canGiveItem(id: StringName): Boolean =
        inventory.contains(id) && entity?.let { e ->
            val en = e.gameWorld?.getEntity(id)!!
            val ic = en.getComponent<ItemComponent>()!!

            !ic.questItem
        } ?: false

    // CURRENCY

    /** Get the amount of this currency this inventory has. */
    @Register
    fun getCurrencyAmount(id: StringName): Int = currencies[id] ?: 0

    /** Set amount of this currency. */
    @Register
    fun setCurrencyAmount(id: StringName, amount: Int) {
        currencies[id] = amount
    }

    /** Get a list of all currencies this inventory has. */
    @Register
    fun getAllCurrencies(): VariantArray<StringName> = currencies.keys.toVariantArray()

    // EQUIPMENT

    /** Try to equip this item to a slot. */
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

    /** Try to unequip an item. */
    @Register
    fun tryUnequip(id: StringName): Boolean {
        if (!equipment.containsValue(id)) return false

        equipment.onEach { (eSlot, item) -> if (item == id) equipment[eSlot] = null }
        equipmentChanged.emit(id, InventoryChangeState.Remove)

        return true
    }

    // TRANSACTIONS

    @Register
    fun tryApplyTransaction(tx: InventoryTransaction): Boolean {
        val accepted = canAcceptTransaction(tx) ?: return false
        acceptTransaction(accepted)
        return true
    }

    /** Try to see if this transaction can be accepted. */
    fun canAcceptTransaction(tx: InventoryTransaction): AcceptedInventoryTransaction? {
        return if (tx.valid(entity?.gameWorld ?: return null) && if (tx.to == entity?.name) {
                // Make sure we can take the items
                tx.items?.let { items -> items.all { id -> canTakeItem(id) }.also { log("can take all items: $it") } } ?: true
            } else { // Take transaction from
                // Make sure we have the items and can give them
                (tx.items?.let { items -> items.all { id -> canGiveItem(id).also { log("can give all items: $it") } } } ?: true)
                        // Make sure we have all the currencies necessary
                        && tx.currencies?.let { c ->
                    c.all { (name, delta) -> currencies.getOrDefault(name, 0) >= delta }
                } ?: true
            }
        ) AcceptedTx(tx)
        else null
    }

    // Transactions are supposed to be atomic to prevent item dupe glitches
    /** Execute an transaction. */
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
        carryWeightCache = null
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