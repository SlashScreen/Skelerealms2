package godot.loot

import godot.annotation.Export
import godot.annotation.FloatRange
import godot.annotation.Script
import godot.core.Dictionary
import godot.core.StringName
import godot.global.GD

@Script
class LootChance : LootTree() {
    @Export
    var success: LootTree? = null
    @Export
    var failure: LootTree? = null
    @FloatRange(0.0f, 1.0f)
    var chance: Float = 1.0f

    override fun resolve(userdata: Dictionary<StringName, Any?>): LootTable {
        return if (GD.randf() <= chance) {
            success?.resolve(userdata) ?: run {
                GD.printErr("Loot table success field must be set.")
                LootTable()
            }
        } else {
            failure?.resolve(userdata) ?: LootTable()
        }
    }
}