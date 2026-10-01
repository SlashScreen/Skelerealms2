class_name LootUniqueItems
extends LootTree

@export var unique_items: Array[StringName]


func resolve(_userdata: Dictionary = { }) -> LootTable:
	return LootTableBuilder.new().with_unique_items(unique_items).build()
