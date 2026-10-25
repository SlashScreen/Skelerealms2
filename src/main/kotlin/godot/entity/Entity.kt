package godot.entity

import godot.GameWorld
import godot.annotation.*
import godot.api.Node
import godot.core.*
import godot.extension.connectLambda
import godot.global.GD

/* TODO
* Prototype link for savegame
* hasComponent: Generic
*/

@Script
class Entity : Node() {
    /** Entity Simulation State. */
    enum class SimState {
        /** Fully simulated, in the scene. */
        FULL,

        /** Partially simulated. Does basic actions. */
        PARTIAL,

        /** Does not get simulated. */
        NONE
    }

    /** Emitted when the room changes. */
    @Emit
    val roomChanged by signal1<StringName>()

    /** Emitted when the sim state changes. */
    @Emit
    val simStateChanged by signal1<SimState>()

    /** The transform for this entity. */
    @Visible
    var transform: Transform3D = Transform3D.IDENTITY

    /** Reference to the GameWorld this Entity is underneath. */
    @Visible
    var gameWorld: GameWorld? = null

    /** References to components. */
    private val components: HashMap<StringName, EntityComponent> = hashMapOf()

    /** Simulation State. */
    @Export
    var simState: SimState = SimState.NONE
        set(value) {
            if (field == value) return

            field = value
            simStateChanged.emit(simState)
        }

    /** The room this entity is in. */
    @Export
    var room: StringName = "".asCachedStringName()
        set(value) {
            if (field == value) return

            field = value
            roomChanged.emit(value)

            gameWorld?.let {
                simState = when {
                    it.currentRoom == value -> SimState.FULL
                    else -> SimState.PARTIAL
                }
            }
        }

    /** Global position of the entity. */
    @Export
    var position: Vector3
        get() = transform.origin
        set(value) {
            transform.origin = value
        }

    /** Euler rotation of this entity. */
    @Export
    var rotation: Vector3
        get() = transform.basis.getEuler()
        set(value) {
            transform.basis = Basis(value)
        }

    /** Quaternion rotation of this entity. */
    @Export
    var quaternion: Quaternion
        get() = transform.basis.getQuaternion()
        set(value) {
            transform.basis = Basis(value)
        }

    override fun _ready() {
        gameWorld?.let { gw ->
            gw.roomChanged.connectLambda { onGameWorldRoomChanged(it) }
            onGameWorldRoomChanged(gw.currentRoom)
        }
    }

    // TODO: Don't be full by default
    private fun onGameWorldRoomChanged(newRoom: StringName) {
        simState = when {
            newRoom == room -> SimState.FULL
            else -> SimState.PARTIAL
        }
    }

    /** Get component, generic. */
    inline fun <reified T> getComponent(): T? where T : EntityComponent {
        val n = StringName(T::class.simpleName ?: "")
        return getComponentByName(n) as? T
    }

    /** Get component by class name. */
    @Register
    fun getComponentByName(n: StringName): EntityComponent? = components[n]

    /** Get component by script class. Used for GDScript. */
    @Register
    fun getComponentByType(script: godot.api.Script): EntityComponent? = components[script.getGlobalName()]

    /** Add component, generic. */
    inline fun <reified T> addComponent(component: T) where T : EntityComponent {
        val n = StringName(T::class.simpleName ?: "")
        if (hasComponent(n)) {
            GD.pushWarning("Attempted to add a component twice: $n")
        } else {
            addComponentByName(n, component)
            addChild(component)
            component.entity = this
        }
    }

    /** Add a component by an already created node. */
    @Register
    fun addComponentByNode(component: EntityComponent) {
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

    /** Add a component by its node and a name. */
    @Register
    fun addComponentByName(cName: StringName, component: EntityComponent) {
        components[cName] = component
    }

    /** Remove a component, generic. */
    inline fun <reified T> removeComponent() = removeComponentByName(StringName(T::class.simpleName ?: ""))

    /** Remove a component by a script class. Used for GDScript. */
    @Register
    fun removeComponentByType(script: godot.api.Script) = removeComponentByName(script.getGlobalName())

    /** Remove a component by name. */
    @Register
    fun removeComponentByName(n: StringName) {
        components[n]?.let { c ->
            components.remove(n)
            c.queueFree()
        }
    }

    /** Does this have a component? */
    @Register
    fun hasComponent(componentName: StringName): Boolean = components.containsKey(componentName)

    /** Add a component by using a prototype. */
    @Register
    fun addComponentFromPrototype(
        prototype: EntityComponentPrototype,
        userdata: Dictionary<StringName, Any?> = Dictionary()
    ) {
        val n = prototype.constructComponent(userdata)
        n.prototype = prototype
        val cName = (n.getScript() as godot.api.Script).getGlobalName()
        n.name = cName

        if (components.containsKey(cName)) {
            GD.pushWarning("Attempted to add a component twice: $n")
        } else {
            components[cName] = n
            addChild(n)
            n.entity = this
        }
    }

    /** Gather this entity's savegame data. */
    @Register
    fun gatherSaveData(): Dictionary<StringName, Dictionary<StringName, Any?>> =
        Dictionary<StringName, Dictionary<StringName, Any?>>().also {
            components.forEach { (_, component) ->
                component.getSaveData()?.let { data -> it[component.saveDataKey()] = data }
            }
            it["entity".asCachedStringName()] =
                dictionaryOf(
                    "tx".asCachedStringName() to transform,
                    "room".asCachedStringName() to room,
                    //"prototype".asCachedStringName() to
                )
        }

    /** Apply save data to this entity. */
    @Register
    fun applySaveData(data: Dictionary<StringName, Dictionary<StringName, Any?>>) {
        // Load entity stuff
        data["entity".asCachedStringName()].let { packet ->
            packet["tx".asCachedStringName()]?.let { tx -> tx as? Transform3D }?.let { tx3D -> transform = tx3D }
            packet["room".asCachedStringName()]?.let { room -> room as? StringName }?.let { room -> this.room = room }
        }

        // Try to apply to each component
        // TODO: Optimize?...
        data.forEach { (id, packet) -> components.values.first { ec -> ec.handlesSaveData(id) }.loadFromData(packet) }
    }

    /** Teleport this entity. */
    @Register
    fun teleport(location: Vector3, room: StringName) {
        this.room = room
        position = location
    }

    /** Log message, giving the entity's name in the printed string. */
    @Register
    fun log(msg: String) {
        GD.print("$name: $msg")
    }
}
