package godot.entity.attributes

import godot.annotation.Script
import godot.annotation.Visible
import godot.core.Dictionary
import godot.core.StringName
import godot.core.asCachedStringName
import godot.entity.EntityComponent

@Script
class AttributesComponent : EntityComponent() {
    @Visible
    val attributes: Dictionary<StringName, Float> = Dictionary()

    override fun getSaveData(): Dictionary<StringName, Any?> =
        Dictionary<StringName, Any?>().apply { attributes.forEach { (name, value) -> set(name, value) } }

    override fun loadFromData(data: Dictionary<StringName, Any?>) {
        attributes.clear()
        data.forEach { (name, value) ->
            (value as? Float)?.let { attributes[name] = it }
        }
    }

    override fun saveDataKey(): StringName = "attributesComponent".asCachedStringName()
}
