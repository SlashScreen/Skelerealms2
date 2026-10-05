package godot.entity

import godot.api.Node
import godot.core.Dictionary
import godot.core.StringName
import godot.core.asCachedStringName
import godot.global.GD

class EntityComponentGd : EntityComponent() {
    var node: Node? = null

    override fun saveDataKey(): StringName =
        node?.call("get_save_data_key".asCachedStringName()) as? StringName ?: run {
            GD.printErr("EntityCOmponentGD needs attached resource with `get_save_data_key()`",)
            "".asCachedStringName()
        }

    override fun getSaveData(): Dictionary<StringName, Any?>? {
        val res = node?.call("get_save_data".asCachedStringName()) as? Dictionary<StringName, Any?> ?: run {
            GD.printErr("EntityCOmponentGD needs attached resource with `get_save_data()`")
            return null
        }
        return if (res.isEmpty()) null else res
    }

    override fun loadFromData(data: Dictionary<StringName, Any?>) {
        node?.call("load_from_data".asCachedStringName(), data) as? StringName
            ?: GD.printErr("EntityCOmponentGD needs attached resource with `get_save_data_key()`")
    }
}
