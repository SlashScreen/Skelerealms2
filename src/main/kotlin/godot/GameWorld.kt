package godot

import godot.annotation.*
import godot.api.Node
import godot.core.*
import godot.entity.Entity
import godot.entity.EntityInstance
import godot.global.GD
import godot.navigation.Navigator

// TODO
// Move rooms to being nodes

@Script
class GameWorld : Node() {
    @Emit
    val roomChanged by signal1<StringName>()

    @Export
    var settings: SKSettings? = null

    @Export
    var registry: EntityRegistry? = null

    @Visible
    var currentRoom: StringName
        get() = room
        set(value) {
            GD.print("Changing Gameworld room to: $value")
            room = value
            roomChanged.emit(value)
        }

    val navigator = Navigator()

    private val entities: HashMap<StringName, Entity> = HashMap()
    private var room: StringName = "".asCachedStringName()

    override fun _ready() {
        registry?.initialize()
        loadAllEntities()

        lambdaCallable0 {
            settings?.let { s ->
                GD.print("Applying settings")
                currentRoom = s.startingRoom
                // TODO: Move player
            }
        }.callDeferred()
    }

    @Register
    fun addEntityFromInstance(instance: EntityInstance, userdata: Dictionary<StringName, Any?> = Dictionary()) {
        val n = instance.constructEntity(userdata)
        if (entities.containsKey(n.name)) {
            GD.printErr("Entity ${n.name} already exists")
            n.queueFree()
            return
        }
        entities[n.name] = n
        n.gameWorld = this
        addChild(n)
        GD.print("Entity ${n.name} added")
    }

    @Register
    fun getEntity(entityName: StringName): Entity? = entities[entityName]

    @Register
    fun entityExists(entityName: StringName): Boolean = entities.containsKey(entityName)

    @Register
    fun gatherSaveData(): Dictionary<StringName, Dictionary<StringName, Dictionary<StringName, Any?>>> =
        Dictionary<StringName, Dictionary<StringName, Dictionary<StringName, Any?>>>().also { dictionary ->
            entities.forEach { (id, entity) ->
                dictionary[id] = entity.gatherSaveData()
            }
        }

    @Register
    fun applySaveData(data: Dictionary<StringName, Dictionary<StringName, Dictionary<StringName, Any?>>>) {
        data.forEach { (key, dictionary) ->
            entities[key]?.applySaveData(dictionary)
        }
    }

    @Register
    fun createSaveGame(): SaveGame = SaveGame().apply { data = gatherSaveData() }

    @Register
    fun applySaveGame(saveData: SaveGame) = applySaveData(saveData.data ?: dictionaryOf())

    @Register
    fun loadAllEntities(userdata: Dictionary<StringName, Any?> = Dictionary()) {
        entities.values.forEach { e -> e.queueFree() }
        entities.clear()

        val instances = registry?.loadAllBlocking() ?: run {
            GD.printErr("Needs entity registry to load entities.")
            return
        }
        instances.values().forEach { instance -> addEntityFromInstance(instance, userdata) }
    }
}
