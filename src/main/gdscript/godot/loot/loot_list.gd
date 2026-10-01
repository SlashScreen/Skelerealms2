class_name LootList
extends LootTree

@export var entries: Array[LootTree]


func resolve(_userdata: Dictionary = { }) -> LootTable:
	return entries.reduce(func(lt: LootTable, next: LootTable) -> LootTable: return lt.merge(next), LootTable.new())
