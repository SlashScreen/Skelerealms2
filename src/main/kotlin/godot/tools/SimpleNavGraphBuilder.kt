package godot.tools

import godot.annotation.Register
import godot.annotation.Script
import godot.annotation.Tool
import godot.api.NavigationMesh
import godot.api.RefCounted
import godot.navigation.SimpleNavGraph

// This is until we get proper tool support although idk if it will work
@Tool
@Script
class SimpleNavGraphBuilder : RefCounted() {
    @Register
    fun buildSimpleNavGraph(mesh: NavigationMesh): SimpleNavGraph = SimpleNavGraph.createFromNavMesh(mesh)
}
