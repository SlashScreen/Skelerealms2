package godot.entity

import godot.api.Node
import godot.api.Resource
import godot.core.Dictionary
import godot.core.StringName
import godot.core.asCachedStringName
import godot.global.GD

class EntityComponentPrototypeGd : EntityComponentPrototype() {
    var resource: Resource? = null

    override fun constructComponent(userdata: Dictionary<StringName, Any?>): EntityComponent =
        (resource?.call("construct_component".asCachedStringName()) as? Node)?.let { EntityComponentGd().apply {
            node = it
            it.set("instance".asCachedStringName(), this)
        } } ?: run {
            GD.printErr("EntityComponentGD needs attached resource with `construct_component()`")
            error("Entity component needs attached resource with `construct_component`")
        }
}
