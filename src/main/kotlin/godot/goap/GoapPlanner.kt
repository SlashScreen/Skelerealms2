package godot.goap

import godot.api.Resource
import godot.core.Dictionary
import godot.core.StringName

class GoapPlanner: Resource() {
    val actions: Array<GoapAction> = arrayOf()

    fun setActions(actions: Array<GoapAction>) {
        actions.copyInto(this.actions)
    }

    public fun getPlan(goal: GoapGoal, blackboard: Dictionary<StringName, Any?>) : Array<GoapAction>? {
        val desiredState = goal.getDesiredState().duplicate(true)
        if (desiredState.isEmpty()) return null

        return findBestPlan(goal, desiredState, blackboard)
    }

    fun findBestPlan(goal: GoapGoal, desiredState: Dictionary<*, *>, blackboard: Dictionary<StringName, Any?>) : Array<GoapAction>? {
        // TODO
        return null
    }

    fun getCheapestPlan(plans: Array<PlanNode>): PlanNode? = plans.minByOrNull { plan -> plan.cost }

    fun buildPlan(step: PlanNode, blackboard: Dictionary<StringName, Any?>): Boolean {
        // TODO
        return true
    }

    fun treeIntoArray(plan: PlanNode, blackboard: Dictionary<StringName, Any?>): Array<GoapAction> {
        // TODO
        return arrayOf()
    }

    class PlanNode {
        public var action: GoapAction? = null
        public var state: Dictionary<*, *>? = null
        public val children: Array<PlanNode> = arrayOf()
        public var cost: Int = 0
    }
}