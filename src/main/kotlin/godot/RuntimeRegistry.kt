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
import godot.global.GD

/** Kotlin wrapper for RuntimeRegistry GDScript class. */
class RuntimeRegistry<T>(private val registry: Resource) where T : Resource {
    init {
        scan()
    }

    /** Load from a string path instead. */
    constructor(path: String) : this(ResourceLoader.load(path) ?: throw Error("No registry at path $path"))

    /** The directory this gets resources from. */
    val scanDir: String
        get() = registry.get("scan_path".asCachedStringName()) as String

    /** All names this registry has. */
    @Suppress("UNCHECKED_CAST")
    val names: VariantArray<StringName>
        get() = registry.get("names".asCachedStringName()) as VariantArray<StringName>

    /** All paths this registry has. May be `res://` or `uid://`. */
    @Suppress("UNCHECKED_CAST")
    val ids: VariantArray<String>
        get() = registry.get("paths".asCachedStringName()) as VariantArray<String>

    @Suppress("UNCHECKED_CAST")
    private val namesToPaths: Dictionary<StringName, String>
        get() = registry.get("_names_to_paths".asCachedStringName()) as Dictionary<StringName, String>

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
        namesToPaths.mapValues { entry ->
            ResourceLoader.loadAs<T>(
                ResourceUID.ensurePath(entry.value),
                typeHint,
                cacheMode
            )!! // TODO: Error out if null?
        }

    /** Check if this registry has an entry. */
    fun hasEntry(name: StringName): Boolean = registry.call("has_entry".asCachedStringName()) as Boolean

    /** Get path for an entry. Null if none exists. */
    fun getPathForEntry(name: StringName): String? =
        (registry.call("get_path_for_entry".asCachedStringName(), name) as String).let { it.ifEmpty { null } }
}
