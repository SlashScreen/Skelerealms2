package godot.goap

import godot.annotation.Script
import godot.api.Resource
import godot.core.Dictionary
import godot.core.StringName
import godot.core.asCachedStringName
import godot.core.dictionaryOf
import godot.global.GD

// Wraps around resource
class GoapAction(val resource: Resource) {

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

    fun getPreconditions(): Dictionary<*, *> =
        when (val res = resource.call("get_preconditions".asCachedStringName())) {
            is Dictionary<*, *> -> res
            else -> dictionaryOf<Any?, Any?>()
        }

    fun getEffects(): Dictionary<*, *> = when (val res = resource.call("get_effects".asCachedStringName())) {
        is Dictionary<*, *> -> res
        else -> dictionaryOf<Any?, Any?>()
    }

    fun setEffects(effects: Dictionary<StringName, Any?>) {
        effects.forEach { (name, value) -> worldState?.setState(name, value) }
    }

    fun enter() = resource.call("enter".asCachedStringName())
    fun exit() = resource.call("exit".asCachedStringName())
    fun perform(delta: Double): Boolean = resource.call("perform".asCachedStringName(), delta) == true
}