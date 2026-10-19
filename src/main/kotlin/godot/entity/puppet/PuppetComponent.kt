package godot.entity.puppet

import godot.annotation.Register
import godot.annotation.Script
import godot.api.Node3D
import godot.api.PackedScene
import godot.core.Dictionary
import godot.core.StringName
import godot.core.Transform3D
import godot.core.asCachedStringName
import godot.entity.Entity.SimState
import godot.entity.EntityComponent
import godot.extension.connectMethod
import godot.global.GD

@Script
class PuppetComponent : EntityComponent() {
    enum class ControlMode {
        EntityControl, // Puppet position is set to entity position
        PuppetControl  // Entity position is set to puppet position
    }

    private val puppetScene: PackedScene?
        get() = prototype?.let { (it as PuppetComponentPrototype).puppetScene }
    private var puppet: Node3D? = null
    var controlMode: ControlMode = ControlMode.PuppetControl // TODO: Change this

    override fun _ready() {
        entity?.simStateChanged?.connectMethod(this, PuppetComponent::trySpawnPuppet)
        entity?.let { trySpawnPuppet(it.simState) }
    }

    override fun _physicsProcess(delta: Double) {
        if (puppet == null) return

        when (controlMode) {
            ControlMode.EntityControl -> entity?.let { e -> puppet?.globalTransform = e.transform }
            ControlMode.PuppetControl -> puppet?.let { p -> entity?.transform = p.globalTransform }
        }
    }

    @Register
    fun trySpawnPuppet(state: SimState) {
        when (state) {
            SimState.FULL -> spawnPuppet()
            else -> destroyPuppet()
        }
    }

    private fun spawnPuppet() {
        log("spawning puppet")
        if (puppet != null) return
        puppetScene?.let { scene ->
            val ps = scene.instantiate() as Node3D
            ps.set("puppet_component".asCachedStringName(), this)
            addChild(ps)
            puppet = ps
            ps.globalTransform = entity?.transform ?: Transform3D.IDENTITY
            entity?.let { ps.name = it.name } // TODO: Figure out something for the generic item names
        } ?: GD.pushError("Puppet scene not found!")
    }

    private fun destroyPuppet() {
        log("destroying puppet")
        puppet?.queueFree()
        puppet = null
    }

    override fun saveDataKey(): StringName = "puppetComponent".asCachedStringName()

    override fun getSaveData(): Dictionary<StringName, Any?>? = null

    override fun loadFromData(data: Dictionary<StringName, Any?>) {}
}
