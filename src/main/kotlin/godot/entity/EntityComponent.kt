package godot.entity

import godot.annotation.Script
import godot.annotation.Visible
import godot.api.Node
import godot.core.Dictionary
import godot.core.StringName

@Script
abstract class EntityComponent: Node() {
    @Visible var entity: Entity? = null
    @Visible var dirty: Boolean = false // Used for savegame stuff. Set to true when loading from a save point or setting a value.

    fun handlesSaveData(key: StringName): Boolean = key == saveDataKey()

    abstract fun saveDataKey(): StringName

    abstract fun loadFromData(data: Dictionary<StringName, Any?>)

    abstract fun getSaveData(): Dictionary<StringName, Any?>?
}
