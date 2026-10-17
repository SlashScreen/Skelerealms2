package godot.entity.attributes

import godot.annotation.Register
import godot.annotation.Script
import godot.annotation.Visible
import godot.core.*
import godot.entity.EntityComponent

@Script
class AttributesComponent : EntityComponent() {
    private val attributes = Dictionary<StringName, Float>()
    private val perks: MutableSet<StringName> = mutableSetOf()

    @Visible
    val allAttributes: Dictionary<StringName, Float>
        get() = instance?.let {
            attributes.duplicate(false).apply { merge((it as AttributesComponentPrototype).attributes) }
        } ?: attributes

    @Visible
    val allPerks: VariantArray<StringName>
        get() = instance?.let { perks.toVariantArray().apply { addAll((it as AttributesComponentPrototype).perks) } }
            ?: perks.toVariantArray()

    @Register
    fun setAttribute(aName: StringName, value: Float) {
        attributes[aName] = value
    }

    @Register
    fun getAttribute(aName: StringName, default: Float = 0f): Float =
        attributes.getOrDefault(aName, null)
            ?: instance?.let { (it as AttributesComponentPrototype).attributes.getOrDefault(aName, null) }
            ?: default

    @Register
    fun addPerk(perk: StringName) {
        perks.add(perk)
    }

    @Register
    fun hasPerk(perk: StringName): Boolean =
        perks.contains(perk) || (instance?.let { (it as AttributesComponentPrototype).perks.has(perk) } ?: false)

    override fun getSaveData(): Dictionary<StringName, Any?> =
        Dictionary<StringName, Any?>().apply {
            set("attributes".asCachedStringName(), attributes)
            set("perks".asCachedStringName(), perks.toVariantArray())
        }

    override fun loadFromData(data: Dictionary<StringName, Any?>) {
        attributes.clear()
        perks.clear()

        data["attributes".asCachedStringName()]?.let { attr ->
            (attr as? Dictionary<StringName, Float>)?.let {
                attributes.putAll(it)
            }
        }
        data["perks".asCachedStringName()]?.let { pks -> (pks as? VariantArray<StringName>)?.let { perks.addAll(it) } }
    }

    override fun saveDataKey(): StringName = "attributesComponent".asCachedStringName()
}
