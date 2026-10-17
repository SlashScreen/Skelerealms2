class_name LootPrototype
extends LootTree

@export var prototype: ItemComponentPrototype


func resolve(_userdata: Dictionary = { }) -> LootTable:
	if prototype == null:
		push_error("SKLootPrototype: Prototype must not be null.")
		return LootTable.new()

	return LootTableBuilder.new().with_prototypes([prototype]).build()
