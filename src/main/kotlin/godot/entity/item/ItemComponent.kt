package godot.entity.item

import godot.annotation.Register
import godot.annotation.Script
import godot.annotation.Visible
import godot.core.StringName
import godot.core.asCachedStringName
import godot.entity.EntityComponent

@Script
class ItemComponent : EntityComponent() {
    public var inventory: StringName? = null
    @Visible
    var containedInventory: StringName
        get() = inventory ?: "".asCachedStringName()
        set(value) {
            inventory = if (inventory.toString().isEmpty()) null else value
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
}
