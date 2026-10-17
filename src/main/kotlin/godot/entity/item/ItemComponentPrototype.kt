package godot.entity.item

import godot.annotation.Export
import godot.annotation.Script
import godot.api.PackedScene
import godot.core.Dictionary
import godot.core.StringName
import godot.core.VariantArray
import godot.core.asCachedStringName
import godot.entity.EntityComponent
import godot.entity.EntityComponentPrototype

@Script
class ItemComponentPrototype : EntityComponentPrototype() {
    /** Leave empty for ID name instead */
    @Export
    var itemName: StringName = "".asCachedStringName()

    @Export
    var model: PackedScene? = null

    @Export
    var baseCurrency: StringName = StringName("")

    @Export
    var baseCost: Int = 0

    @Export
    var weight: Float = 1f

    @Export
    var questItem: Boolean = false

    @Export
    var tags: VariantArray<StringName> = VariantArray()

    @Export
    var equipmentSlots: VariantArray<StringName> = VariantArray()

    val tagsSet: Set<StringName>
        get() = internalTagsSet ?: tags.toSet().also { internalTagsSet = it }
    private var internalTagsSet: Set<StringName>? = null

    override fun constructComponent(userdata: Dictionary<StringName, Any?>): EntityComponent = ItemComponent()
}
