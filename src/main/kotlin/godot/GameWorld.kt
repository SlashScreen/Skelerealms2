package godot

import godot.annotation.*
import godot.api.Node
import godot.api.Resource
import godot.core.*
import godot.entity.Entity
import godot.entity.EntityInstance
import godot.global.GD
import godot.navigation.Navigator

@Script
class GameWorld : Node() {
    /** Emitted when the game's world changes. */
    @Emit
    val roomChanged by signal1<StringName>()

    /** Settings object. */
    @Export
    var settings: SKSettings? = null

    /** RuntimeRegistry to back the internal registry. */
    @HintString("RuntimeRegistry")
    var runtimeRegistry: Resource? = null

    /** Registry reference. */
    private val registry: SaveGameRegistry
        get() {
            if (!this::lateSaveGameRegistry.isInitialized) {
                lateSaveGameRegistry = SaveGameRegistry(
                    RuntimeRegistry<EntityInstance>(
                        runtimeRegistry ?: throw Error("GameWorld must have backing registry")
                    ).apply { scan() }, null // TODO: savegame
                )
            }
            return lateSaveGameRegistry
        }
    private lateinit var lateSaveGameRegistry: SaveGameRegistry

    /** The current room that this world is in. */
    @Visible
    var currentRoom: StringName
        get() = room
        set(value) {
            GD.print("Changing Gameworld room to: $value")
            room = value
            roomChanged.emit(value)
            registry.getAllEntitiesInRoom(value).forEach { addEntityByName(it) }
        }

    /** Entities reference map. */
    private val entities: HashMap<StringName, Entity> = HashMap()

    /** Internal room value. */
    private var room: StringName = "".asCachedStringName()

    override fun _ready() {
        lambdaCallable0 {
            settings?.let { s ->
                GD.print("Applying settings")
                currentRoom = s.startingRoom
                // TODO: Move player
            }
        }.callDeferred()
    }

    /** Ask the game to load an entity. */
    @Register
    fun addEntityByName(id: StringName, userdata: Dictionary<StringName, Any?> = Dictionary()) {
        val (eInstance, saveData) = registry.loadEntityInstance(id) ?: return
        val entity = addEntityFromInstance(eInstance, userdata) ?: return
        saveData?.let { entity.applySaveData(it) }
    }

    /** Load an entity from an instance directly. */
    @Register
    fun addEntityFromInstance(
        instance: EntityInstance,
        userdata: Dictionary<StringName, Any?> = Dictionary()
    ): Entity? {
        val n = instance.constructEntity(userdata)
        if (entities.containsKey(n.name)) {
            GD.pushWarning("Entity ${n.name} already exists")
            n.queueFree()
            return null
        }
        entities[n.name] = n
        n.gameWorld = this
        addChild(n)
        GD.print("Entity ${n.name} added")
        return n
    }

    /** Get an entity, if it's loaded. */
    @Register
    fun getEntity(entityName: StringName): Entity? = entities[entityName]

    /** Does this entity exist? */
    @Register
    fun entityExists(entityName: StringName): Boolean = entities.containsKey(entityName)

    /** Delete entity - it will never spawn or be created again. */
    @Register
    fun deleteEntity(entityName: StringName) {
        entities[entityName]?.let { e ->
            e.queueFree()
            entities.remove(entityName)
            registry.deleteEntity(entityName)
        }
    }

    /** Gather the savegame data for all entities. */
    @Register
    fun gatherSaveData(): Dictionary<StringName, Dictionary<StringName, Dictionary<StringName, Any?>>> =
        Dictionary<StringName, Dictionary<StringName, Dictionary<StringName, Any?>>>().also { dictionary ->
            entities.forEach { (id, entity) ->
                dictionary[id] = entity.gatherSaveData()
            }
        }

    /** Apply savegame data for all loaded entities. */
    @Register
    fun applySaveData(data: Dictionary<StringName, Dictionary<StringName, Dictionary<StringName, Any?>>>) {
        data.forEach { (key, dictionary) ->
            entities[key]?.applySaveData(dictionary)
        }
    }

    /** Create a SaveGame for this GameWorld. */
    @Register
    fun createSaveGame(): SaveGame = SaveGame().apply {
        data = gatherSaveData()
        deletedEntities = registry.deletedEntities.toVariantArray()
    }

    /** Apply a SaveGame to this GameWorld. */
    @Register
    fun applySaveGame(saveData: SaveGame) =
        applySaveData(saveData.data) // TODO: How do we go about this? Reset the registry?
}
