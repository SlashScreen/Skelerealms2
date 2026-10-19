package godot.entity

import godot.annotation.Export
import godot.annotation.Script
import godot.annotation.Visible
import godot.api.Node
import godot.core.Dictionary
import godot.core.StringName

@Script
abstract class EntityComponent : Node() {
    /** The parent entity. */
    @Visible
    var entity: Entity? = null

    /** Used for savegame stuff. May be deprecated. */
    @Visible
    var dirty: Boolean =
        false // Used for savegame stuff. Set to true when loading from a save point or setting a value.

    /** The backing instance for this component. */
    @Export
    var prototype: EntityComponentPrototype? = null

    /** Does this component handle this blob? */
    fun handlesSaveData(key: StringName): Boolean = key == saveDataKey()

    fun log(msg: String) {
        entity?.log("($name) $msg")
    }

    /** What key in the save data blob should be given to this component. */
    abstract fun saveDataKey(): StringName

    /** Load using the data blob. */
    abstract fun loadFromData(data: Dictionary<StringName, Any?>)

    /** Gather save data blob. */
    abstract fun getSaveData(): Dictionary<StringName, Any?>?
}
