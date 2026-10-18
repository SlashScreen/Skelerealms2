package godot.entity

import godot.annotation.Export
import godot.annotation.Register
import godot.annotation.Script
import godot.annotation.Tool
import godot.annotation.Visible
import godot.api.Resource
import godot.core.*
import godot.global.GD

@Script
class EntityInstance : Resource() {
    // TODO: Will probably have issues with editor vs constructor instantiating
    @Export
    var id: StringName = "".asCachedStringName() //(1..RANDOM_NAME_LENGTH).map { allowedChars.random() }.joinToString("").asCachedStringName()

    @Export var prototype: EntityPrototype? = null

    @Export
    var room: StringName = "".asCachedStringName()

    @Visible
    var transform: Transform3D = Transform3D.IDENTITY

    @Export
    var position: Vector3
        get() = transform.origin
        set(value) {
            transform.origin = value
        }

    @Export
    var rotation: Vector3
        get() = transform.basis.getEuler()
        set(value) {
            transform.basis = Basis(value)
        }

    @Export
    var quaternion: Quaternion
        get() = transform.basis.getQuaternion()
        set(value) {
            transform.basis = Basis(value)
        }

    @Register
    fun constructEntity(userdata: Dictionary<StringName, Any?> = Dictionary()): Entity {
        val n = Entity()
        n.name = id
        n.room = room
        n.transform = transform
        prototype?.components?.map { r ->
            when (r) {
                is EntityComponentPrototype -> r
                else -> EntityComponentPrototypeGd().apply { resource = r } // TODO: Validate?
            }
        }?.forEach { c -> n.addComponentFromInstance(c, userdata) }
        GD.print("Entity $id constructed!")
        return n
    }

    companion object {
        private val allowedChars = ('A'..'F').plusElement('0'..'9')
        private const val RANDOM_NAME_LENGTH = 8
    }
}