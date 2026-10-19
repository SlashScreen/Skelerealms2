extends RigidBody3D

## Set by puppet component host
var puppet_component: PuppetComponent


func _on_interactible_interacted(who: StringName) -> void:
	print("Attempt to add to inventory")

	var e: Entity = puppet_component.entity
	var gw: GameWorld = e.game_world

	var to_e: Entity = gw.get_entity(who)
	if not to_e:
		push_warning("%s is not an entity" % who)
		return

	var ec := to_e.get_component_by_type(InventoryComponent)
	var ic: InventoryComponent = ec as InventoryComponent
	if not ic:
		push_warning("%s has no inventory component" % who)
		return

	var tx := InventoryTransaction.new().with_to(who).with_items([e.name])
	if ic.try_apply_transaction(tx):
		print("Applied transaction")
	else:
		print("Failed to apply transaction")
