package godot.entity.attributes

import godot.annotation.Register
import godot.annotation.Script
import godot.annotation.Visible
import godot.core.Dictionary
import godot.core.StringName
import godot.core.asCachedStringName
import godot.entity.EntityComponent

@Script
class AttributesComponent : EntityComponent() {
    private val attributes = Dictionary<StringName, Float>()

    @Visible
    val allAttributes: Dictionary<StringName, Float>
        get() = instance?.let { i ->
            attributes.duplicate(false).apply { merge((i as AttributesComponentPrototype).attributes) }
        } ?: attributes

    @Register
    fun setAttribute(aName: StringName, value: Float) {
        attributes[aName] = value
    }

    @Register
    fun getAttribute(aName: StringName): Float =
        attributes.getOrDefault(aName, null)
            ?: instance?.let { i -> (i as AttributesComponentPrototype).attributes.getOrDefault(aName, null) }
            ?: 0F

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
