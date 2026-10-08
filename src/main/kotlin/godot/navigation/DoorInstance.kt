package godot.navigation

import godot.annotation.Export
import godot.annotation.Script
import godot.api.Resource
import godot.core.Quaternion
import godot.core.StringName
import godot.core.Vector3
import godot.core.asCachedStringName

@Script
class DoorInstance : Resource() {
    @Export
    var position: Vector3 = Vector3()

    @Export
    var rotation: Quaternion = Quaternion()

    @Export
    var room: StringName = "".asCachedStringName()

    @Export
    var otherSide: DoorInstance? = null
}
