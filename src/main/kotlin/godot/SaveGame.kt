package godot

import godot.annotation.Script
import godot.annotation.Storage
import godot.api.Resource
import godot.core.Dictionary
import godot.core.StringName
import godot.core.VariantArray
import godot.core.variantArrayOf

@Script
class SaveGame : Resource() {
    /** Save game data for entities */
    @Storage
    var data: Dictionary<StringName, Dictionary<StringName, Dictionary<StringName, Any?>>> = Dictionary()

    /** Entities flagged as deleted. */
    @Storage
    var deletedEntities: VariantArray<StringName> = variantArrayOf()

    // TODO:
    // Game stats/info
}
