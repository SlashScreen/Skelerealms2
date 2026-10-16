package godot.entity.inventory

import godot.annotation.Export
import godot.annotation.Script
import godot.core.Dictionary
import godot.core.StringName
import godot.entity.EntityComponent
import godot.entity.EntityComponentInstance
import godot.loot.LootTree

@Script
class InventoryComponentInstance : EntityComponentInstance() {
    @Export
    var lootTree: LootTree? = null

    override fun constructComponent(userdata: Dictionary<StringName, Any?>): EntityComponent =
        InventoryComponent().also { component -> component.lootTree = lootTree }
}
