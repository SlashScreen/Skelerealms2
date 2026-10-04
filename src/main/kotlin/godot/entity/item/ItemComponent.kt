package godot.entity.item

import godot.annotation.Register
import godot.annotation.Script
import godot.annotation.Visible
import godot.core.Dictionary
import godot.core.StringName
import godot.core.asCachedStringName
import godot.core.dictionaryOf
import godot.entity.EntityComponent
import godot.global.GD

@Script
class ItemComponent : EntityComponent() {
    var inventory: StringName? = null

    @Visible
    public var containedInventory: StringName
        get() = inventory ?: "".asCachedStringName()
        set(value) {
            inventory = if (value.toString().isEmpty()) null else value
        }

    @Visible
    public var prototype: ItemPrototype? = null

    @Visible
    public val inInventory: Boolean
        get() = inventory != null

    @Register
    public fun buildFromInstance(res: ItemComponentInstance) {
        prototype = res.prototype
    }

    @Register
    override fun handlesSaveData(key: StringName): Boolean = key == "itemComponent".asCachedStringName()

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
