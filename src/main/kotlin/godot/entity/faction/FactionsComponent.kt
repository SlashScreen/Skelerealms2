package godot.entity.faction

import godot.annotation.Register
import godot.annotation.Script
import godot.core.Dictionary
import godot.core.StringName
import godot.core.VariantArray
import godot.core.asCachedStringName
import godot.entity.EntityComponent

@Script
class FactionsComponent : EntityComponent() {
    private val factions: Dictionary<StringName, Long> = Dictionary()

    @Register
    fun isInFaction(fName: StringName): Boolean = factions.containsKey(fName)

    @Register
    fun getFactionRank(fName: StringName): Long = factions.get(fName, 0L)!!

    @Register
    fun setFactionRank(fName: StringName, rank: Long) = factions.put(fName, rank)

    @Register
    fun getFactions(): VariantArray<StringName> = factions.keys()

    override fun getSaveData(): Dictionary<StringName, Any?> =
        Dictionary<StringName, Any?>().apply { factions.forEach { set(it.key, it.value) } }

    override fun loadFromData(data: Dictionary<StringName, Any?>) {
        factions.clear()
        data.forEach { (fName, rank) -> (rank as? Long)?.let { factions[fName] = it } }
    }

    override fun saveDataKey(): StringName = "factions".asCachedStringName()
}
