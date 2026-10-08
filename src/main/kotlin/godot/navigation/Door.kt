package godot.navigation

import godot.annotation.Export
import godot.annotation.Register
import godot.annotation.Script
import godot.api.Marker3D
import godot.core.StringName

@Script
class Door: Marker3D() {
    @Export var instance: DoorInstance? = null

    @Register
    fun open(who: StringName) {
        TODO("Not yet implemented")
    }

    // TODO: Tooling
}
