package godot.entity

import godot.annotation.Export
import godot.annotation.HintString
import godot.annotation.Register
import godot.annotation.Script
import godot.annotation.Visible
import godot.api.Resource
import godot.core.*
import godot.global.GD

@Script
class EntityInstance : Resource() {
    // TODO: Will probably have issues with editor vs constructor instantiating
    @Export
    var id: StringName = "".asCachedStringName() //(1..RANDOM_NAME_LENGTH).map { allowedChars.random() }.joinToString("").asCachedStringName()

    @HintString("24/17:EntityComponentInstance,EntityComponentInstanceGD")
    var components: VariantArray<Resource> = VariantArray()

    @Export
    public var room: StringName = "".asCachedStringName()

    @Visible
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
        n.room = room
        n.transform = transform
        components.map { r ->
            when (r) {
                is EntityComponentInstance -> r
                else -> EntityComponentInstanceGd().apply { resource = r } // TODO: Validate?
            }
        }.forEach { c -> n.addComponentFromInstance(c, userdata) }
        GD.print("Entity $id constructed!")
        return n
    }

    companion object {
        val allowedChars = ('A'..'F').plusElement('0'..'9')
        const val RANDOM_NAME_LENGTH = 8
    }
}