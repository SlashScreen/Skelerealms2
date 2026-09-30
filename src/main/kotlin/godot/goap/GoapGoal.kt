package godot.goap

import godot.api.Resource
import godot.core.Dictionary
import godot.core.StringName
import godot.core.asCachedStringName
import godot.core.dictionaryOf
import godot.global.GD

class GoapGoal(val resource: Resource) {

    var costCache: Int? = null
    public val cost: Int
        get() = costCache ?: when (val res = resource.get("cost".asCachedStringName())) {
            is Int -> res
            else -> {
                GD.printErr("Goap Goal must have int cost value")
                0
            }
        }.also { costCache = it }

    // Do we cache this?
    var enabledCache: Boolean? = null
    public val enabled: Boolean
        get() = enabledCache ?: when (val res = resource.get("enabled".asCachedStringName())) {
            is Boolean -> res
            else -> {
                GD.printErr("Goap Goal must have boolean enabled value")
                false
            }
        }.also { enabledCache = it }

    var priorityCache: Int? = null
    public val priority: Int
        get() = priorityCache ?: when (val res = resource.get("priority".asCachedStringName())) {
            is Int -> res
            else -> {
                GD.printErr("Goap Goal must have int cost value")
                0
            }
        }.also { priorityCache = it }

    var agentCache: GoapAgent? = null
    public val agent: GoapAgent?
        get() = agentCache ?: when (val res = resource.get("agent".asCachedStringName())) {
            is GoapAgent -> res
            else -> {
                GD.printErr("Goap Goal must have agent reference")
                null
            }
        }.also { agentCache = it }

    public val goal: GoapGoal?
        get() = agent?.currentGoal

    var worldStateCache: GoapWorldState? = null
    public val worldState: GoapWorldState?
        get() = worldStateCache ?: when (val res = resource.get("world_state".asCachedStringName())) {
            is GoapWorldState -> res
            else -> {
                GD.printErr("Goap Goal must have world state renference")
                null
            }
        }.also { worldStateCache = it }

    fun getActionName(): StringName = resource.call("get_action_name".asCachedStringName()) as StringName

    fun isValid(): Boolean = resource.call("is_valid".asCachedStringName()) as Boolean

    fun getCost(userdata: Dictionary<StringName, Any?>): Int =
        resource.call("get_cost".asCachedStringName(), userdata) as Int

    fun getPriority(userdata: Dictionary<StringName, Any?>): Int =
        resource.call("get_cost".asCachedStringName(), userdata) as Int

    fun getDesiredState(): Dictionary<*, *> =
        when (val res = resource.call("get_desired_state".asCachedStringName())) {
            is Dictionary<*, *> -> res
            else -> dictionaryOf<Any?, Any?>()
        }

    fun setDesiredState(state: Dictionary<StringName, Any?>): Dictionary<*, *> =
        when (val res = resource.call("set_desired_state".asCachedStringName(), state)) {
            is Dictionary<*, *> -> res
            else -> dictionaryOf<Any?, Any?>()
        }

    fun enter() = resource.call("enter".asCachedStringName())
    fun exit() = resource.call("exit".asCachedStringName())
    fun perform(delta: Double) = resource.call("perform".asCachedStringName(), delta)
    fun onGoalAchieved() = resource.call("on_goal_achieved".asCachedStringName())
    fun onGoalFailed() = resource.call("on_goal_failed".asCachedStringName())
}