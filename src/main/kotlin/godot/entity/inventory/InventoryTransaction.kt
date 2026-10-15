package godot.entity.inventory

import godot.annotation.Script
import godot.api.RefCounted
import godot.core.Dictionary
import godot.core.StringName
import godot.core.VariantArray

@Script
class InventoryTransaction : RefCounted() {
    var from: StringName? = null
    var to: StringName? = null

    var items: VariantArray<StringName>? = null
    var currencies: Dictionary<StringName, Int>? = null

    fun valid(): Boolean {
        return from != null
                && to != null
                && from != to
    }
}