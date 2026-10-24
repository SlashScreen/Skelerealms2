package godot

import godot.api.Resource
import godot.api.ResourceLoader
import godot.api.ResourceUID
import godot.core.Dictionary
import godot.core.StringName
import godot.core.VariantArray
import godot.core.asCachedStringName
import godot.core.godotValue
import godot.extension.loadAs

/** Kotlin wrapper for RuntimeRegistry GDScript class. */
class RuntimeRegistry<T>(private val registry: Resource) where T : Resource {
    /** Load from a string path instead. */
    constructor(path: String) : this(ResourceLoader.load(path) ?: throw Error("No registry at path $path"))

    /** The directory this gets resources from. */
    val scanDir: String
        get() = registry.get("scan_path".asCachedStringName()) as String

    /** All names this registry has. */
    @Suppress("UNCHECKED_CAST")
    val names: VariantArray<StringName>
        get() = registry.get("names".asCachedStringName()) as VariantArray<StringName>

    /** All UIDs this registry has. */
    @Suppress("UNCHECKED_CAST")
    val ids: VariantArray<Long>
        get() = registry.get("ids".asCachedStringName()) as VariantArray<Long>

    @Suppress("UNCHECKED_CAST")
    private val namesToIds: Dictionary<StringName, Long>
        get() = registry.get("_names_to_ids".asCachedStringName()) as Dictionary<StringName, Long>

    /** Scan a directory and catalogue all within. */
    fun scan(directory: String = scanDir) {
        registry.call("scan".asCachedStringName(), directory)
    }

    /** Load an entry from name, with optional hint string and cache mode. Returns null if none found. */
    @Suppress("UNCHECKED_CAST")
    fun loadEntry(
        name: StringName,
        typeHint: String = "",
        cacheMode: ResourceLoader.CacheMode = ResourceLoader.CacheMode.REUSE
    ): T? = registry.call("load_entry".asCachedStringName(), name, typeHint, cacheMode.godotValue) as? T?

    /** Load all entries, blocking. */
    fun loadAllBlocking(
        typeHint: String = "",
        cacheMode: ResourceLoader.CacheMode = ResourceLoader.CacheMode.REUSE
    ): Map<StringName, T> =
        namesToIds.mapValues { entry ->
            ResourceLoader.loadAs<T>(
                ResourceUID.uidToPath(ResourceUID.idToText(entry.value)),
                typeHint,
                cacheMode
            )!! // TODO: Error out if null?
        }

    /** Check if this registry has an entry. */
    fun hasEntry(name: StringName): Boolean = registry.call("has_entry".asCachedStringName()) as Boolean

    /** Get UID for an entry. Null if none exists. */
    fun getIdForEntry(name: StringName): Long? =
        (registry.call("get_id_for_entry".asCachedStringName(), name) as Long).let { if (it == -1L) null else it }
}
