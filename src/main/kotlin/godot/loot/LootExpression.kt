package godot.loot

import godot.annotation.Export
import godot.annotation.MultilineText
import godot.annotation.Script
import godot.api.Expression
import godot.core.Dictionary
import godot.core.PackedStringArray
import godot.core.StringName
import godot.core.variantArrayOf
import godot.global.GD

@Script
class LootExpression : LootTree() {
    @Export
    var success: LootTree? = null
    @Export
    var failure: LootTree? = null
    @MultilineText
    var expression: String = ""
    val expr = Expression().apply {
        parse(
            expression,
            PackedStringArray(variantArrayOf("userdata"))
        )
    } // Will this work? no clue...

    override fun resolve(userdata: Dictionary<StringName, Any?>): LootTable {
        val res = expr.execute(variantArrayOf(userdata))

        if (expr.hasExecuteFailed()) {
            GD.printErr("Error executing script `$expression`: ${expr.getErrorText()}")
            return LootTable()
        }

        if (res is Boolean) {
            return if (res) {
                success?.resolve(userdata) ?: run {
                    GD.printErr("Loot table success field must be set.")
                    LootTable()
                }
            } else {
                failure?.resolve(userdata) ?: LootTable()
            }
        } else {
            GD.printErr("Error executing script `$expression`: result must be a boolean value")
            return LootTable()
        }
    }
}