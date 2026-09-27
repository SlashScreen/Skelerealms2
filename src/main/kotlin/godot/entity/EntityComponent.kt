package godot.entity

import godot.annotation.Script
import godot.annotation.Visible
import godot.api.Node

@Script
abstract class EntityComponent: Node() {
    @Visible var entity: Entity? = null
}
