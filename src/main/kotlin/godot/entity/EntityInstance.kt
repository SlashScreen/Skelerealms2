package godot.entity

import godot.annotation.Export
import godot.annotation.Register
import godot.api.Resource
import godot.core.*

class EntityInstance : Resource() {
    // TODO: Will probably have issues with editor vs constructor instantiating
    @Export
    var id: StringName = StringName((1..RANDOM_NAME_LENGTH).map { allowedChars.random() }.joinToString(""))
    @Export
    var components: VariantArray<EntityComponentInstance> = VariantArray()

    public var transform: Transform3D = Transform3D.IDENTITY

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

    @Register
    public fun constructEntity(userdata: Dictionary<StringName, Any?> = Dictionary()): Entity {
        val n = Entity()
        n.name = id
        components.forEach { c -> n.addComponentFromInstance(c, userdata) }
        return n
    }

    companion object {
        val allowedChars = ('A'..'F').plusElement('0'..'9')
        const val RANDOM_NAME_LENGTH = 8
    }
}