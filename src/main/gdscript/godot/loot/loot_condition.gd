class_name LootCondition
extends LootTree

@export var success: LootTree ## Loot table on success. Mandatory.
@export var failure: LootTree ## Loot table on failure. Optional.
## This is the expression it evaluates. Should return bool.
## Userdata passed through the resolve function is passed in here as [c]data[/c], a dictionary.
@export var expression: String


func resolve(userdata: Dictionary = { }) -> LootTable:
	var expr := Expression.new()
	expr.parse(expression, PackedStringArray(["data"]))

	var res = expr.execute([userdata])
	if expr.has_execute_failed():
		push_error("SKLootCondition `%s` failed - %s" % [expression, expr.get_error_text()])
		return LootTable.new()
	if not res is bool:
		push_error("SKLootCondition: result of `%s` should be a boolean." % expression)
		return LootTable.new()

	if res:
		if not success:
			push_error("SKLootChance: Success node must not be null.")
			return LootTable.new()

		return success.resolve(userdata)
	else:
		if failure:
			return failure.resolve(userdata)
		else:
			return LootTable.new()
