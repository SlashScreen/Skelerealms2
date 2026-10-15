package godot.entity

import godot.GameWorld
import godot.annotation.*
import godot.api.Node
import godot.core.*
import godot.extension.connectLambda
import godot.global.GD

@Script
class Entity : Node() {
    enum class SimState { FULL, PARTIAL, NONE }

    @Emit
    val roomChanged by signal1<StringName>()

    @Emit
    val simStateChanged by signal1<SimState>()

    @Visible
    var transform: Transform3D = Transform3D.IDENTITY

    @Visible
    var gameWorld: GameWorld? = null

    val components: HashMap<StringName, EntityComponent> = hashMapOf()

    @Export
    var simState: SimState = SimState.NONE
        set(value) {
            if (field == value) return
            GD.print("$name: set sim state: $value")

            field = value
            simStateChanged.emit(simState)
        }

    @Export
    var room: StringName = "".asCachedStringName()
        set(value) {
            if (field == value) return
            GD.print("$name: set room to $value")

            field = value
            roomChanged.emit(value)

            gameWorld?.let {
                simState = when {
                    it.currentRoom == value -> SimState.FULL
                    else -> SimState.PARTIAL
                }
            }
        }

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

    override fun _ready() {
        gameWorld?.let { gw ->
            gw.roomChanged.connectLambda { onGameWorldRoomChanged(it) }
            onGameWorldRoomChanged(gw.room)
        }
    }

    private fun onGameWorldRoomChanged(newRoom: StringName) {
        GD.print("$name: onGameWorldRoomChanged() called with: newRoom = $newRoom (in room $room)")
        simState = when {
            newRoom == room -> SimState.FULL
            else -> SimState.PARTIAL
        }
    }

    // TODO: Don't be full by default

    public inline fun <reified T> getComponent(): T? where T : EntityComponent {
        val n = StringName(T::class.simpleName ?: "")
        return components[n] as? T
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
    public fun addComponentFromInstance(
        instance: EntityComponentInstance,
        userdata: Dictionary<StringName, Any?> = Dictionary()
    ) {
        val n = instance.constructComponent(userdata)
        val cName = (n.getScript() as godot.api.Script).getGlobalName()
        n.name = cName
        addComponentByNode(n) // gets name twice, but...
    }

    @Register
    public fun gatherSaveData(): Dictionary<StringName, Dictionary<StringName, Any?>> =
        Dictionary<StringName, Dictionary<StringName, Any?>>().also {
            components.forEach { (_, component) ->
                component.getSaveData()?.let { data -> it[component.saveDataKey()] = data }
            }
            it["entity".asCachedStringName()] =
                dictionaryOf("tx".asCachedStringName() to transform, "room".asCachedStringName() to room)
        }

    @Register
    public fun applySaveData(data: Dictionary<StringName, Dictionary<StringName, Any?>>) {
        // Load entity stuff
        data["entity".asCachedStringName()].let { packet ->
            packet["tx".asCachedStringName()]?.let { tx -> tx as? Transform3D }?.let { tx3D -> transform = tx3D }
            packet["room".asCachedStringName()]?.let { room -> room as? StringName }?.let { room -> this.room = room }
        }

        // Try to apply to each component
        // TODO: Optimize?...
        data.forEach { (id, packet) -> components.values.first { ec -> ec.handlesSaveData(id) }.loadFromData(packet) }
    }

    @Register
    public fun teleport(location: Vector3, room: StringName) {
        this.room = room
        position = location
    }
}
