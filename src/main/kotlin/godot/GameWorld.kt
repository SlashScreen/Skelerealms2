package godot

import godot.annotation.*
import godot.api.Marshalls
import godot.api.Node
import godot.core.*
import godot.entity.Entity
import godot.entity.EntityInstance
import godot.global.GD

@Script
class GameWorld : Node() {
    @Export
    var registry: EntityRegistry? = null
    var room: StringName = "".asCachedStringName()
    val entities: HashMap<StringName, Entity> = HashMap()

    @Visible
    var currentRoom: StringName
        get() = room
        set(value) {
            room = value
            roomChanged.emit(room)
        }

    @Emit
    val roomChanged by signal1<StringName>()

    @Register
    public fun addEntityFromInstance(instance: EntityInstance, userdata: Dictionary<StringName, Any?> = Dictionary()) {
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
    public fun getEntity(entityName: StringName): Entity? = entities[entityName]

    @Register
    public fun entityExists(entityName: StringName): Boolean = entities.containsKey(entityName)

    @Register
    public fun gatherSaveData(): Dictionary<StringName, Dictionary<StringName, Dictionary<StringName, Any?>>> =
        Dictionary<StringName, Dictionary<StringName, Dictionary<StringName, Any?>>>().also { dictionary ->
            entities.forEach { (id, entity) ->
                dictionary[id] = entity.gatherSaveData()
            }
        }

    @Register
    public fun applySaveData(data: Dictionary<StringName, Dictionary<StringName, Dictionary<StringName, Any?>>>) {
        data.forEach { (key, dictionary) ->
            entities[key]?.applySaveData(dictionary)
        }
    }

    @Register
    public fun createSaveGame(): SaveGame = SaveGame().apply { data = gatherSaveData() }

    @Register
    public fun applySaveGame(saveData: SaveGame) = applySaveData(saveData.data ?: dictionaryOf())

    @Register
    public fun loadAllEntities(userdata: Dictionary<StringName, Any?> = Dictionary()) {
        entities.values.forEach { e -> e.queueFree() }
        entities.clear()

        val instances = registry?.loadAllBlocking() ?: run {
            GD.printErr("Needs entity registry to load entities.")
            return
        }
        instances.values().forEach { instance -> addEntityFromInstance(instance, userdata) }
    }

    override fun _ready() {
        registry?.initialize()
        loadAllEntities()
    }
}
