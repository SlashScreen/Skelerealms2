package godot

import godot.annotation.Dir
import godot.annotation.Register
import godot.annotation.Script
import godot.api.Resource
import godot.api.ResourceLoader
import godot.api.ResourceUID
import godot.core.*
import godot.entity.EntityInstance
import godot.extension.loadAs

@Script
class EntityRegistry : Resource() {
    @Dir
    var resPath: String = ""

    val ids: HashMap<StringName, Long> = hashMapOf()
    var cache: HashMap<StringName, EntityInstance> = hashMapOf()
    val regex = Regex(PATTERN)

    // Operates on state
    fun fillFromPath(path: String) {
        val paths = ResourceLoader.listDirectory(path).map { p -> path + p }

        paths.forEach { path ->
            if (path.endsWith('/')) {
                fillFromPath(path)
            } else {
                val uid = ResourceLoader.getResourceUid(path)
                // If valid, try match, try load.
                if (uid != -1L) regex.matchEntire(path)?.let { matchResult ->
                    matchResult.groupValues.getOrNull(1)?.let { ids[it.asCachedStringName()] = uid }
                }
            }
        }
    }

    @Register
    public fun initialize() {
        fillFromPath(resPath)
    }

    @Register
    public fun load(name: StringName): EntityInstance? = ids[name]?.let { uid ->
        cache[name] ?: ResourceLoader.loadAs<EntityInstance>(ResourceUID.getIdPath(uid), "EntityInstance")
            ?.also { entityInstance -> cache[name] = entityInstance }
    }

    @Register
    public fun getAllStringIDs(): VariantArray<StringName> = variantArrayOf(*ids.keys.toTypedArray())

    @Register
    public fun loadAllBlocking(): Dictionary<StringName, EntityInstance> {
        val dictionary = Dictionary<StringName, EntityInstance>()
        ids.forEach { (name, uid) ->
            cache[name]?.let { entityInstance -> dictionary[name] = entityInstance }
                ?: ResourceLoader.loadAs<EntityInstance>(ResourceUID.getIdPath(uid), "EntityInstance")
                    ?.also { entityInstance -> cache[name] = entityInstance }
                    ?.let { dictionary[name] = it }
        }
        return dictionary
    }

    @Register
    public fun exists(name: StringName): Boolean = ids.containsKey(name)

    @Register
    public fun getId(name: StringName): Long = ids[name] ?: -1

    companion object {
        const val PATTERN = """.*/(.*)\.(?:res|tres)"""
    }
}
