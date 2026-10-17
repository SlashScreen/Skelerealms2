package godot.world

import godot.annotation.Export
import godot.annotation.Script
import godot.api.Marker3D
import godot.entity.EntityInstance

/** The main class for entity markers, that allows you to place entities directly into the world */
@Script
class EntityMarker : Marker3D() {
    /*
    * TODO: Tooling
    *  Create item
    *  Create NPC
    *  Model preview
    *  Sync position
    * */

    @Export
    var instance: EntityInstance? = null
}
