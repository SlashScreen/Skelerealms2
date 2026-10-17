package godot.entity.item

import godot.annotation.Export
import godot.annotation.Register
import godot.annotation.Script
import godot.annotation.Visible
import godot.api.PackedScene
import godot.core.Dictionary
import godot.core.StringName
import godot.core.asCachedStringName
import godot.core.dictionaryOf
import godot.entity.EntityComponent
import godot.global.GD

@Script
class ItemComponent : EntityComponent() {
    private var inventory: StringName? = null

    @Visible
    public var containedInventory: StringName
        get() = inventory ?: "".asCachedStringName()
        set(value) {
            inventory = if (value.toString().isEmpty()) null else value
        }

    @Export
    var model: PackedScene? = instance?.let { (it as ItemComponentPrototype).model }
    @Export
    var baseCurrency: StringName = instance?.let { (it as ItemComponentPrototype).baseCurrency } ?: StringName("")
    @Export
    var baseCost: Int = instance?.let { (it as ItemComponentPrototype).baseCost } ?: 0
    @Export
    var weight: Float = instance?.let { (it as ItemComponentPrototype).weight } ?: 0F

    @Visible
    public val inInventory: Boolean
        get() = inventory != null

    // TODO: Safeguard this
    fun addToInventory(id: StringName) {
        inventory = id
    }

    @Register
    override fun saveDataKey(): StringName = "itemComponent".asCachedStringName()

    @Register
    override fun loadFromData(data: Dictionary<StringName, Any?>) {
        containedInventory = data["containedInventory".asCachedStringName()]?.let { it as? StringName } ?: run {
            GD.printErr("Invalid save data blob for ItemComponent")
            return
        }
    }

    @Register
    override fun getSaveData(): Dictionary<StringName, Any?> {
        return dictionaryOf(
            "containedInventory".asCachedStringName() to containedInventory
        )
    }
}
