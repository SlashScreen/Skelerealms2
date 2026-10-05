package godot

import godot.annotation.Script
import godot.annotation.Visible
import godot.api.Resource
import godot.core.Dictionary
import godot.core.StringName

@Script
class SaveGame: Resource() {
    @Visible var data: Dictionary<StringName, Dictionary<StringName, Dictionary<StringName, Any?>>>? = null
}
