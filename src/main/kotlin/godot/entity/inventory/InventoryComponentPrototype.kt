package godot.entity.inventory

import godot.annotation.Export
import godot.annotation.FloatRange
import godot.annotation.HintString
import godot.annotation.Range
import godot.annotation.Script
import godot.api.Resource
import godot.core.Dictionary
import godot.core.StringName
import godot.entity.EntityComponent
import godot.entity.EntityComponentPrototype
import godot.loot.LootTree
import godot.loot.LootTreeGd

@Script
class InventoryComponentPrototype : EntityComponentPrototype() {
    @HintString("LootTree")
    var lootTreeGd: Resource? = null
    val lootTree: LootTree?
        get() = lootTreeInstance
            ?: lootTreeGd?.let { lt -> LootTreeGd().apply { resource = lt } }
                .also { lootTreeInstance = it }
    private var lootTreeInstance: LootTreeGd? = null

    @FloatRange(min = 0.0, max = 1.0, or = Range.OR_GREATER)
    var maxCarryWeight = 100F

    override fun constructComponent(userdata: Dictionary<StringName, Any?>): EntityComponent =
        InventoryComponent()
}
