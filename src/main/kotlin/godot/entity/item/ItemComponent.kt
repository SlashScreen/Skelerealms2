package godot.entity.item

import godot.annotation.Register
import godot.annotation.Script
import godot.annotation.Visible
import godot.api.PackedScene
import godot.core.*
import godot.entity.EntityComponent
import godot.global.GD

@Script
class ItemComponent : EntityComponent() {

    @Visible
    var containedInventory: StringName
        get() = inventory ?: "".asCachedStringName()
        set(value) {
            inventory = if (value.toString().isEmpty()) null else value
        }

    @Visible
    val model: PackedScene?
        get() = instance?.let { (it as ItemComponentPrototype).model }

    @Visible
    val baseCurrency: StringName
        get() = instance?.let { (it as ItemComponentPrototype).baseCurrency } ?: StringName("")

    @Visible
    val baseCost: Int
        get() = instance?.let { (it as ItemComponentPrototype).baseCost } ?: 0

    @Visible
    val weight: Float
        get() = instance?.let { (it as ItemComponentPrototype).weight } ?: 0F

    @Visible
    val questItem: Boolean
        get() = internalQuestItem ?: instance?.let { (it as ItemComponentPrototype).questItem } ?: false

    @Visible
    val equipmentSlots: VariantArray<StringName>
        get() = instance?.let { (it as ItemComponentPrototype).equipmentSlots } ?: VariantArray()

    val equipmentSlotsSet: Set<StringName>
        get() = internalEquipmentSlots
            ?: instance?.let { (it as ItemComponentPrototype).equipmentSlots.toSet() }
                .also { internalEquipmentSlots = it }
            ?: emptySet()

    @Visible
    val inInventory: Boolean
        get() = inventory != null

    private var internalQuestItem: Boolean? = null
    private var internalEquipmentSlots: Set<StringName>? = null
    private var inventory: StringName? = null
    private val tags: MutableSet<StringName> = mutableSetOf()

    // TODO: Safeguard this
    fun addToInventory(id: StringName) {
        inventory = id
    }

    @Register
    fun hasTag(tag: StringName): Boolean =
        tags.contains(tag) || instance?.let { (it as ItemComponentPrototype).tags.contains(tag) } ?: false

    @Register
    fun addTag(tag: StringName) {
        tags.add(tag)
    }

    @Register
    override fun saveDataKey(): StringName = "itemComponent".asCachedStringName()

    @Register
    override fun loadFromData(data: Dictionary<StringName, Any?>) {
        containedInventory = data["containedInventory".asCachedStringName()]?.let { it as? StringName } ?: run {
            GD.printErr("Invalid save data blob for ItemComponent")
            return
        }
        data["tags".asCachedStringName()]
            ?.let { (it as? VariantArray<StringName>) }
            ?.let { tgs -> tags.addAll(tgs) }
            ?: run {
                GD.printErr("Invalid save data blob for ItemComponent")
                return
            }
    }

    @Register
    override fun getSaveData(): Dictionary<StringName, Any?> {
        return dictionaryOf(
            "containedInventory".asCachedStringName() to containedInventory,
            "tags".asCachedStringName() to tags.toVariantArray(),
        )
    }
}
