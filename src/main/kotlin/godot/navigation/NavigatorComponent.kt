import godot.annotation.Emit
import godot.annotation.Register
import godot.annotation.Script
import godot.annotation.Visible
import godot.core.Dictionary
import godot.core.StringName
import godot.core.Vector3
import godot.core.asCachedStringName
import godot.core.dictionaryOf
import godot.core.signal1
import godot.core.signal2
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
}
