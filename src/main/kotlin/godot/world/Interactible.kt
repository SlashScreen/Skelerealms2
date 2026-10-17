package godot.world

import godot.annotation.*
import godot.api.Area3D
import godot.api.Node
import godot.core.StringName
import godot.core.asCachedStringName
import godot.core.signal1

@Script
class Interactible : Area3D() {
    @Emit
    val interacted by signal1<StringName>() // arg is who

    @Export
    var verbTranslationKey: StringName = "interact".asCachedStringName()

    @Visible
    val verb: String
        get() = tr(verbTranslationKey)

    @Export
    var customTranslationKey: StringName? = null // set by item or whatever

    @Export
    var inheritNameFrom: Node? = null // TODO: Figure out some item thing

    @Visible
    val translatedName: String
        get() = customTranslationKey?.let { tr(it) }
            ?: inheritNameFrom?.let { tr(it.name) }
            ?: "UNKNOWN"

    @Register
    fun interact(who: StringName) {
        interacted.emit(who)
    }
}
