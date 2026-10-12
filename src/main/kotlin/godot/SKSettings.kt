package godot

import godot.annotation.Export
import godot.annotation.Script
import godot.api.Resource
import godot.core.StringName
import godot.core.Vector3
import godot.core.asCachedStringName

@Script
class SKSettings: Resource() {
    @Export var startingRoom: StringName = "".asCachedStringName()
    @Export var startingPosition: Vector3 = Vector3()
}
