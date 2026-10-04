package godot.goap

import godot.api.Resource
import godot.core.Dictionary
import godot.core.StringName

class GoapPlanner : Resource() {
    val actions: Array<GoapAction> = arrayOf()

    public fun setActions(actions: Array<GoapAction>) {
        actions.copyInto(this.actions)
    }

    public fun getPlan(goal: GoapGoal, blackboard: Dictionary<StringName, Any?>): Array<GoapAction>? {
        val desiredState = goal.getDesiredState().duplicate(true)
        if (desiredState.isEmpty()) return null

        return findBestPlan(goal, desiredState, blackboard)
    }

    fun findBestPlan(
        goal: GoapGoal,
        desiredState: Dictionary<*, *>,
        blackboard: Dictionary<StringName, Any?>
    ): Array<GoapAction>? {
        val p = PlanNode(null, HashMap(desiredState))

        return if (buildPlans(p, blackboard.duplicate(true))) {
            getCheapestPlan(treeIntoArray(p, blackboard))
        } else null
    }

    fun getCheapestPlan(plans: Array<Pair<Array<GoapAction>, Int>>): Array<GoapAction>? =
        plans.minByOrNull { plan -> plan.second }?.first

    fun buildPlans(step: PlanNode, blackboard: Dictionary<StringName, Any?>): Boolean {
        // Drain satisfied values
        val state = HashMap(step.state).filter { (k, v) -> !GoapWorldState.isSatisfied(blackboard[k], v) }

        // If state is empty, we've finished. Else, try continuing to build plans
        return state.isEmpty() || (actions.map { action ->
            val effects = action.getEffects()
            val desiredState = HashMap(state)
            val matchedEffects = desiredState.filter { (k, v) -> v == effects[k] }

            return if (matchedEffects.isNotEmpty()) { // If it should use action
                matchedEffects.keys.forEach { k -> desiredState.remove(k) } // Drain matched effects

                val preconditions = action.getPreconditions()
                // Modify next state
                val nextState = HashMap(desiredState)
                // Add preconditions so long as there are no conflicts
                if (preconditions.any { (k, v) ->
                        nextState[k]?.let { it != v }
                            ?: run {
                                nextState[k] = v
                                false
                            }
                    }
                ) return false

                val p = PlanNode(action, nextState)
                // If the next state is satisfied, or we can keep building the graph
                if (nextState.isEmpty() || buildPlans(p, blackboard.duplicate(true))) {
                    step.children.add(p)
                    true
                } else {
                    false
                }
            } else
                false
        }.any { it })
    }

    fun treeIntoArray(plan: PlanNode, blackboard: Dictionary<StringName, Any?>): Array<Pair<Array<GoapAction>, Int>> {
        // TODO: I think this produces a bunch of GC garbage. Gotta figure out a better way to do this.

        val plans = mutableListOf<Pair<Array<GoapAction>, Int>>()

        if (plan.children.isEmpty()) {
            return arrayOf(Pair(plan.action?.let { arrayOf(it) } ?: arrayOf(),
                plan.action?.getCost(blackboard) ?: 0)) // TODO: Userdata?
        }

        plan.children.forEach { child ->
            for ((first, second) in treeIntoArray(child, blackboard)) {
                plans.add(
                    Pair(
                        plan.action?.let { first + it } ?: first,
                    second + (plan.action?.getCost(blackboard) ?: 0)
                ))
            }
        }

        return plans.toTypedArray()
    }

    class PlanNode(var action: GoapAction?, var state: HashMap<Any?, Any?>) {
        public val children: MutableList<PlanNode> = mutableListOf()
        public var cost: Int = 0
    }
}