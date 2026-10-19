extends CharacterBody3D


const SPEED = 5.0


## The nav agent.
@export var agent: NavigationAgent3D

## The puppet component, set by the puppet component
var puppet_component: PuppetComponent


## Set the target for the agent.
func set_world_target(target: Vector3) -> void:
	agent.target_position = target


func _ready() -> void:
	var e := puppet_component.entity
	if e == null:
		return

	var nc := e.get_component_by_type(NavigatorComponent) as NavigatorComponent

	nc.world_target_changed.connect(set_world_target)
	nc.test_pos_set()


func _physics_process(_delta: float) -> void:
	if agent.is_navigation_finished():
		return

	var next_pos := agent.get_next_path_position()
	var dir := (next_pos - global_position).normalized()

	velocity = dir * SPEED

	move_and_slide()
