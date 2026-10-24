package godot

import godot.api.ResourceLoader
import godot.core.Dictionary
import godot.core.StringName
import godot.core.Transform3D
import godot.core.asCachedStringName
import godot.entity.EntityInstance
import godot.extension.loadAs
import godot.global.GD

/** Entity registry with an optional save game layer on top. */
class SaveGameRegistry(private val backingRegistry: RuntimeRegistry<EntityInstance>, private val saveGame: SaveGame?) {
    /** Loaded entity instances. */
    private val entities = backingRegistry.loadAllBlocking() // I do not like this

    /** Does this entity exist? */
    fun hasEntity(id: StringName): Boolean = (saveGame?.data?.has(id) ?: false) || backingRegistry.hasEntry(id)

    /** Load an entity instance, and save data to apply if applicable. Null if no entity exists. */
    fun loadEntityInstance(id: StringName): Pair<EntityInstance, Dictionary<StringName, Dictionary<StringName, Any?>>?>? {
        return saveGame?.let { sg -> // If we have a savegame
            sg.data.getOrDefault(id, null)?.let { // If save data has this entity
                // Build instance
                val instance = EntityInstance().apply {
                    val eInfo = it["entity".asCachedStringName()]
                    // Set basic values
                    this.id = id
                    transform = eInfo["transform".asCachedStringName()] as Transform3D
                    room = eInfo["room".asCachedStringName()] as StringName
                    // Load prototype
                    prototype = ResourceLoader.loadAs(eInfo["prototype".asCachedStringName()] as String)
                        ?: return null.also { GD.pushError("Could not load prototype from save file for entity $id") }
                }
                Pair(instance, it)
            }
        } ?: entities[id]?.let { Pair(it, null) }
    }

    /** Get all entities in a room */
    fun getAllEntitiesInRoom(room: StringName): List<StringName> {
        // Get immutable entities in this room
        val immutableEntities = entities.mapNotNull { kv -> if (kv.value.room == room) kv.key else null }

        saveGame?.let { sg ->
            // Get save data entities in this room
            val saveEntitiesInRoom =
                sg.data.mapNotNull { kv -> if (kv.value["entity".asCachedStringName()]["room".asCachedStringName()] == room) kv.key else null }
            // Filter out immutable entities whose rooms have been modified
            val nonOverwrittenImmutableEntities = immutableEntities.filter {
                if (sg.data.has(it)) { // if we have it in the save data
                    getSaveGameEntityRoom(
                        sg,
                        it
                    )?.let { r -> r != room } // filter out those whose room is not this room
                        ?: true // we're good although this should be unreachable
                } else true // we're good
            }
            return saveEntitiesInRoom.union(nonOverwrittenImmutableEntities).toList()
        }

        return immutableEntities
    }

    private fun getSaveGameEntityRoom(sg: SaveGame, name: StringName): StringName? {
        if (!sg.data.has(name)) return null
        return sg.data[name]["entity".asCachedStringName()]["room".asCachedStringName()] as StringName
    }
}
