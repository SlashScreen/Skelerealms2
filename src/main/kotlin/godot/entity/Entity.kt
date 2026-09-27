package godot.entity

import godot.annotation.Export
import godot.annotation.Register
import godot.annotation.Script
import godot.api.Node
import godot.core.*
import godot.global.GD

@Script
class Entity : Node() {
    public var transform: Transform3D = Transform3D.IDENTITY
    val components: HashMap<StringName, EntityComponent> = hashMapOf()

    @Export
    public var position: Vector3
        get() = transform.origin
        set(value) {
            transform.origin = value
        }

    @Export
    public var rotation: Vector3
        get() = transform.basis.getEuler()
        set(value) {
            transform.basis = Basis(value)
        }

    @Export
    public var quaternion: Quaternion
        get() = transform.basis.getQuaternion()
        set(value) {
            transform.basis = Basis(value)
        }

    public inline fun <reified T> getComponent(): EntityComponent? where T : EntityComponent {
        val n = StringName(T::class.simpleName ?: "")
        return components[n]
    }

    @Register
    public fun getComponentByName(n: StringName): EntityComponent? {
        return components[n]
    }

    @Register
    public fun getComponentByType(script: godot.api.Script): EntityComponent? {
        return components[script.getGlobalName()]
    }

    public inline fun <reified T> addComponent(component: T) where T : EntityComponent {
        val n = StringName(T::class.simpleName ?: "")
        if (components.containsKey(n)) {
            GD.pushWarning("Attempted to add a component twice: $n")
        } else {
            components[n] = component
            addChild(component)
            component.entity = this
        }
    }

    @Register
    public fun addComponentByNode(component: EntityComponent) {
        val s = component.getScript() as godot.api.Script
        val n = s.getGlobalName()
        if (components.containsKey(n)) {
            GD.pushWarning("Attempted to add a component twice: $n")
        } else {
            components[n] = component
            addChild(component)
            component.entity = this
        }
    }

    public inline fun <reified T> removeComponent() = removeComponentByName(StringName(T::class.simpleName ?: ""))

    @Register
    public fun removeComponentByType(script: godot.api.Script) = removeComponentByName(script.getGlobalName())

    @Register
    public fun removeComponentByName(n: StringName) {
        components[n]?.let { c ->
            components.remove(n)
            c.queueFree()
        }
    }

    @Register
    public fun addComponentFromInstance(instance: EntityComponentInstance, userdata: Dictionary<StringName, Any?> = Dictionary()) {
        val n = instance.constructComponent(userdata)
        val cName = (n.getScript() as godot.api.Script).getGlobalName()
        n.name = cName
        addComponentByNode(n) // gets name twice, but...
    }
}
