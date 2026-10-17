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
import godot.global.GD

@Script
class EntityRegistry : Resource() {
    @Dir
    var resPath: String = ""

    private val ids: HashMap<StringName, Long> = hashMapOf()
    private var cache: HashMap<StringName, EntityInstance> = hashMapOf()
    private val regex = Regex(PATTERN)

    // Operates on state
    private fun fillFromPath(path: String) {
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
    fun initialize() {
        fillFromPath(resPath)
    }

    @Register
    fun load(name: StringName): EntityInstance? = ids[name]?.let { uid ->
        cache[name] ?: ResourceLoader.loadAs<EntityInstance>(ResourceUID.getIdPath(uid), "EntityInstance")
            ?.also { entityInstance -> cache[name] = entityInstance }
    }

    @Register
    fun getAllStringIDs(): VariantArray<StringName> = variantArrayOf(*ids.keys.toTypedArray())

    @Register
    fun loadAllBlocking(): Dictionary<StringName, EntityInstance> {
        val dictionary = Dictionary<StringName, EntityInstance>()
        ids.forEach { (name, uid) ->
            GD.print("Loading $name for $uid")
            cache[name]?.let { entityInstance -> dictionary[name] = entityInstance }
                ?: ResourceLoader.loadAs<EntityInstance>(ResourceUID.getIdPath(uid), "EntityInstance")
                    ?.also { entityInstance -> cache[name] = entityInstance }
                    ?.let { dictionary[name] = it }
        }
        return dictionary
    }

    @Register
    fun exists(name: StringName): Boolean = ids.containsKey(name)

    @Register
    fun getId(name: StringName): Long = ids[name] ?: -1

    companion object {
        private const val PATTERN = """.*/(.*)\.(?:res|tres)"""
    }
}
