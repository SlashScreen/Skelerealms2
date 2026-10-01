class_name LootChance
extends LootTree

@export_range(0.0, 1.0) var chance: float = 0.5 ## Chance of success, 1.0 being 100%.
@export var success: LootTree ## Loot node on success. Mandatory.
@export var failure: LootTree ## Loot table on failure. Optional.


func resolve(userdata: Dictionary = { }) -> LootTable:
	var c := randf()

	if c <= chance:
		if not success:
			push_error("SKLootChance: Success node must not be null.")
			return LootTable.new()

		return success.resolve(userdata)
	else:
		if failure:
			return failure.resolve(userdata)
		else:
			return LootTable.new()
