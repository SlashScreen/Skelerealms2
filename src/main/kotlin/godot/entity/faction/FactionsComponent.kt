package godot.entity.faction

import godot.annotation.Register
import godot.annotation.Script
import godot.core.*
import godot.entity.EntityComponent

@Script
class FactionsComponent : EntityComponent() {
    private val factions: HashMap<StringName, Long> = HashMap()

    @Register
    fun isInFaction(fName: StringName): Boolean =
        factions.containsKey(fName) || (instance?.let { i -> (i as FactionsComponentPrototype).factions.containsKey(fName) }
            ?: false)

    @Register
    fun getFactionRank(fName: StringName): Long =
        factions[fName] ?: instance?.let { i -> (i as FactionsComponentPrototype).factions.get(fName, null) } ?: 0L

    @Register
    fun setFactionRank(fName: StringName, rank: Long) = factions.put(fName, rank)

    @Register
    fun getFactions(): VariantArray<StringName> = factions.keys.apply {
        instance?.let { i -> (i as FactionsComponentPrototype).factions.keys() }?.let { fs -> addAll(fs) }
    }.toVariantArray()

    override fun getSaveData(): Dictionary<StringName, Any?> =
        Dictionary<StringName, Any?>().apply { factions.forEach { set(it.key, it.value) } }

    override fun loadFromData(data: Dictionary<StringName, Any?>) {
        factions.clear()
        data.forEach { (fName, rank) -> (rank as? Long)?.let { factions[fName] = it } }
    }

    override fun saveDataKey(): StringName = "factions".asCachedStringName()
}
