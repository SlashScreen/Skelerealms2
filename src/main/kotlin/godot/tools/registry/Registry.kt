package godot.tools.registry

import godot.api.GodotInstance
import godot.api.ResourceLoader
import godot.api.Resource
import godot.core.Dictionary
import godot.core.GodotObject
import godot.core.StringName
import godot.core.VariantArray
import godot.core.asCachedStringName
import godot.core.asStringName
import godot.core.godotValue
import godot.core.lambdaCallable1
import godot.extension.loadAs

class Registry<T>(res: Resource) where T : Resource {
    val size: Int
        get() = registry.call("size".asCachedStringName()) as Int

    private val registry: Resource = res

    constructor(path: String) : this(ResourceLoader.load(path) ?: throw Error("No registry at path $path"))

    fun has(id: StringName): Boolean = registry.call("has".asCachedStringName(), id) as Boolean

    fun hasStringId(id: StringName): Boolean = registry.call("has_string_id".asCachedStringName(), id) as Boolean

    fun hasUid(id: StringName): Boolean = registry.call("has_uid".asCachedStringName(), id) as Boolean

    fun getUid(id: StringName): StringName? = (registry.call("get_uid".asCachedStringName(), id) as StringName).let {
        if (it.toString().isNotEmpty()) it else null
    }

    fun getStringId(id: StringName): StringName? =
        (registry.call("get_string_id".asCachedStringName(), id) as StringName).let {
            if (it.toString().isNotEmpty()) it else null
        }

    fun getStringIdOf(res: Resource): StringName? =
        (registry.call("get_string_id_of".asCachedStringName(), res) as StringName).let {
            if (it.toString().isNotEmpty()) it else null
        }

    @Suppress("UNCHECKED_CAST")
    fun getAllStringIds(): VariantArray<StringName> =
        registry.call("get_all_string_ids".asCachedStringName()) as VariantArray<StringName>

    @Suppress("UNCHECKED_CAST")
    fun getAllUids(): VariantArray<StringName> =
        registry.call("get_all_uids".asCachedStringName()) as VariantArray<StringName>

    @Suppress("UNCHECKED_CAST")
    fun getIndexedProperties(): VariantArray<StringName> =
        registry.call("get_indexed_properties".asCachedStringName()) as VariantArray<StringName>

    fun isEmpty(): Boolean = registry.call("is_empty".asCachedStringName()) as Boolean

    fun isPropertyIndexed(property: StringName): Boolean =
        registry.call("is_property_indexed".asCachedStringName(), property) as Boolean

    @Suppress("UNCHECKED_CAST")
    fun filter(property: StringName, criterion: Any?): VariantArray<StringName> =
        registry.call("filter".asCachedStringName(), criterion) as VariantArray<StringName>

    @Suppress("UNCHECKED_CAST")
    fun filter(property: StringName, criterion: (Any?) -> Boolean): VariantArray<StringName> =
        registry.call("filter".asCachedStringName(), lambdaCallable1(criterion)) as VariantArray<StringName>

    @Suppress("UNCHECKED_CAST")
    fun where(criteria: Dictionary<StringName, Any?>): VariantArray<StringName> =
        registry.call("where".asCachedStringName(), criteria) as VariantArray<StringName>

    @Suppress("UNCHECKED_CAST")
    fun loadEntry(
        id: StringName,
        typeHint: String = "",
        cacheMode: ResourceLoader.CacheMode = ResourceLoader.CacheMode.REUSE
    ): T? =
        registry.call("load_entry".asCachedStringName(), id, typeHint, cacheMode.godotValue) as? T?

    @Suppress("UNCHECKED_CAST")
    fun loadAllBlocking(
        typeHint: String = "",
        cacheMode: ResourceLoader.CacheMode = ResourceLoader.CacheMode.REUSE
    ): Dictionary<StringName, T> =
        registry.call(
            "load_all_blocking".asCachedStringName(),
            typeHint,
            cacheMode.godotValue
        ) as Dictionary<StringName, T>

    fun loadAllThreadedRequest(
        typeHint: String = "",
        useSubThreads: Boolean = false,
        cacheMode: ResourceLoader.CacheMode = ResourceLoader.CacheMode.REUSE
    ): RegistryLoadTracker<T> =
        RegistryLoadTracker(
            registry.call(
                "load_all_threaded_request".asCachedStringName(),
                typeHint,
                useSubThreads,
                cacheMode.godotValue
            ) as GodotInstance
        )

    class RegistryLoadTracker<T>(private val tracker: GodotInstance) where T : Resource {
        val progress: Float
            get() = tracker.get("progress".asCachedStringName()) as Float

        @Suppress("UNCHECKED_CAST")
        val resources: Dictionary<StringName, T>
            get() = tracker.get("resources".asStringName()) as Dictionary<StringName, T> // Registry.cs:297 - ???

        @Suppress("UNCHECKED_CAST")
        val requested: Dictionary<StringName, Boolean>
            get() = tracker.get("requested".asCachedStringName()) as Dictionary<StringName, Boolean>

        @Suppress("UNCHECKED_CAST")
        val status: Dictionary<StringName, ResourceLoader.ThreadLoadStatus>
            get() = tracker.get("status".asCachedStringName()) as Dictionary<StringName, ResourceLoader.ThreadLoadStatus>
    }

    companion object {
        const val PROPERTY_HINT_CUSTOM = 1024
    }
}
