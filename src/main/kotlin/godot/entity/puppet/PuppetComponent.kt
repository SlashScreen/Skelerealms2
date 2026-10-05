package godot.entity.puppet

import godot.annotation.Script
import godot.api.Node3D
import godot.api.PackedScene
import godot.core.Dictionary
import godot.core.StringName
import godot.core.Transform3D
import godot.core.asCachedStringName
import godot.entity.Entity
import godot.entity.EntityComponent
import godot.extension.connectLambda
import godot.global.GD

@Script
class PuppetComponent : EntityComponent() {
    var puppetScene: PackedScene? = null
    var puppet: Node3D? = null

    override fun _ready() {
        entity?.simStateChanged?.connectLambda { simState ->
            when (simState) {
                "full".asCachedStringName() -> spawnPuppet()
                else -> destroyPuppet()
            }
        }
    }

    fun spawnPuppet() {
        if (puppet != null) destroyPuppet()
        puppetScene?.let { scene ->
            val ps = scene.instantiate() as Node3D
            addChild(ps)
            puppet = ps
            ps.globalTransform = entity?.transform ?: Transform3D.IDENTITY
        } ?: GD.pushError("Puppet scene not found!")
    }

    fun destroyPuppet() {
        puppet?.queueFree()
        puppet = null
    }

    override fun saveDataKey(): StringName = "puppetComponent".asCachedStringName()

    override fun getSaveData(): Dictionary<StringName, Any?>? = null

    override fun loadFromData(data: Dictionary<StringName, Any?>) {}
}