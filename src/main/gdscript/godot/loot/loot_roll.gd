class_name LootRoll
extends LootTree

@export var items: Array[LootTree]


func resolve(userdata: Dictionary = { }) -> LootTable:
	if items.size() == 0:
		push_error("SKLootRoll: Must have one or more items to pick between.")
		return LootTable.new()

	return items.pick_random().resolve(userdata)
