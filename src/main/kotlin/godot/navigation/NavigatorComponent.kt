package godot.navigation

import godot.annotation.Emit
import godot.annotation.Register
import godot.annotation.Script
import godot.annotation.Visible
import godot.core.*
import godot.entity.Entity
import godot.entity.EntityComponent
import godot.global.GD

@Script
class NavigatorComponent : EntityComponent() {
    @Emit
    val targetChanged by signal2<StringName, Vector3>()

    @Emit
    val worldTargetChanged by signal1<Vector3>()

    var target: Pair<StringName, Vector3>? = null
        set(value) {
            if (field == value) return

            value?.let { v ->
                // Make a new path if we are going somewhere
                entity?.let { e ->
                    currentRoomPath = navigator
                        ?.findPath(field ?: Pair(e.room, e.position), v)
                        ?.toMutableList()
                }
            }

            field = value
            targetChanged.emit(value?.first ?: "".asCachedStringName(), value?.second ?: Vector3.ZERO)
        }

    @Visible
    var targetWorld: StringName
        get() = target?.first ?: "".asCachedStringName()
        set(value) {
            if (value == "".asCachedStringName()) target = null
            else
                target?.let { (_, pos) -> target = Pair(value, pos) }
                    ?: run { target = Pair(value, Vector3.ZERO) }
        }

    @Visible
    var targetPos: Vector3
        get() = target?.second ?: Vector3.ZERO
        set(value) {
            target?.let { (world, _) -> target = Pair(world, value) }
        }

    private var currentRoomPath: MutableList<Navigator.PathStep>? = null
    private var currentPointsPath: PackedVector3Array? = null

    private val navigator: Navigator?
        get() = Navigator.instance
    private val moveSpeed = 2.0
    private val currentStep: Navigator.PathStep?
        get() = currentRoomPath?.lastOrNull()
    private val currentMoveTarget: Vector3?
        get() = currentPointsPath?.lastOrNull()

    override fun _process(delta: Double) {
        when (entity?.simState ?: Entity.SimState.NONE) {
            Entity.SimState.FULL -> {
                currentMoveTarget?.let { mvTarget ->
                    entity?.let { e ->
                        if (e.position.distanceTo(mvTarget) < MIN_DIST) {
                            nextPoint()
                        }
                    }
                }
            }

            Entity.SimState.PARTIAL -> {
                currentMoveTarget?.let { mvTarget ->
                    val pos = entity?.position ?: return

                    val moved = pos.moveToward(mvTarget, moveSpeed * delta)
                    entity?.position = moved

                    if (moved.distanceTo(mvTarget) < MIN_DIST) {
                        nextPoint()
                    }
                }
            }

            else -> {}
        }
    }

    @Register
    fun setTargetWorldAndPos(world: StringName, pos: Vector3) {
        target = Pair(world, pos)
    }

    @Register
    fun clearTarget() {
        target = null
    }

    @Register
    fun testPosSet() {
        val vec = Vector3(GD.randf(), GD.randf(), GD.randf())
        val pos = vec * 30F
        worldTargetChanged.emit(pos)
    }

    private fun nextStep() {
        currentRoomPath?.removeLast()
        currentRoomPath = if (currentRoomPath.isNullOrEmpty()) null else currentRoomPath
        if (currentRoomPath == null) return

        when (val cs = currentStep) {
            is Navigator.AbsoluteStep -> {
                // Execute immediately
                entity?.room = cs.room
                entity?.position = cs.pos
                nextStep()
            }

            is Navigator.PointsPathStep -> {
                currentPointsPath = cs.path
                currentPointsPath?.reverse()
            }

            null -> {}
        }
    }

    private fun nextPoint() {
        currentPointsPath?.let { it.removeAt(it.size - 1) }
        if (currentPointsPath?.isEmpty() ?: true) {
            currentPointsPath = null
            nextStep() // do we put this here?...
        } else {
            currentMoveTarget?.let { worldTargetChanged.emit(it) }
        }
    }

    override fun getSaveData(): Dictionary<StringName, Any?>? = target?.let {
        dictionaryOf(
            "targetWorld".asCachedStringName() to it.first,
            "targetPos".asCachedStringName() to it.second,
        )
    }

    override fun loadFromData(data: Dictionary<StringName, Any?>) {
        val w = data["targetWorld".asCachedStringName()]!! as StringName
        val p = data["targetPos".asCachedStringName()]!! as Vector3
        target = Pair(w, p)
    }

    override fun saveDataKey(): StringName = "navigatorComponent".asCachedStringName()

    companion object {
        private const val MIN_DIST = 0.01
    }
}
