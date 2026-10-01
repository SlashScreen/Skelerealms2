class_name SKLootPrototype
extends LootTree

@export var prototype: ItemPrototype


func resolve(_userdata: Dictionary = { }) -> LootTable:
	if prototype == null:
		push_error("SKLootPrototype: Prototype must not be null.")
		return LootTable.new()

	return LootTableBuilder.new().with_prototypes([prototype]).build()
