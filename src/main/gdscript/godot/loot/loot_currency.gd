class_name LootCurrency
extends LootTree

@export var currency: StringName
@export_range(0, 100, 1, "or_greater") var min_val: int
@export_range(0, 100, 1, "or_greater") var max_val: int


func resolve(_userdata: Dictionary = { }) -> LootTable:
	if max_val < min_val:
		push_error("SKLootCurrency: max chance value must be greater than min chance value")
		return LootTable.new()

	return LootTableBuilder.new().with_currencies({ currency: randi_range(min_val, max_val) }).build()
