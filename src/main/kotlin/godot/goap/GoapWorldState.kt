package godot.goap

import godot.annotation.Emit
import godot.annotation.Register
import godot.annotation.Script
import godot.api.Resource
import godot.core.StringName
import godot.core.signal0

@Script
class GoapWorldState : Resource() {
    var state: HashMap<StringName, Any?> = hashMapOf()

    @Emit
    val stateUpdated by signal0()

    @Register
    public fun getState(key: StringName, default: Any? = null): Any? = state.getOrDefault(key, default)

    @Register
    public fun setState(key: StringName, value: Any?) {
        state[key] = value
        stateUpdated.emit()
    }

    @Register
    public fun eraseState(key: StringName) {
        state.remove(key)
        stateUpdated.emit()
    }

    @Register
    public fun clearState() {
        state.clear()
        stateUpdated.emit()
    }

    companion object {
        public fun isSatisfied(blackboardValue: Any?, desiredValue: Any?): Boolean {
            return if (desiredValue is Boolean) {
                val asBool = blackboardValue?.let {
                    when (it) {
                        is Boolean -> it
                        is Int, is Float -> it != 0
                        else -> true
                    }
                }
                asBool == desiredValue
            } else {
                blackboardValue == null
            }
        }
    }
}