package godot.entity.inventory

import godot.annotation.HintString
import godot.annotation.Script
import godot.api.Resource
import godot.core.Dictionary
import godot.core.StringName
import godot.entity.EntityComponent
import godot.entity.EntityComponentInstance
import godot.loot.LootTree
import godot.loot.LootTreeGd

@Script
class InventoryComponentInstance : EntityComponentInstance() {
    @HintString("LootTree")
    var lootTreeGd: Resource? = null
    val lootTree: LootTree?
        get() = lootTreeInstance
            ?: lootTreeGd?.let { lt -> LootTreeGd().apply { resource = lt } }
                .also { lootTreeInstance = it }

    private var lootTreeInstance: LootTreeGd? = null

    override fun constructComponent(userdata: Dictionary<StringName, Any?>): EntityComponent =
        InventoryComponent()
}
