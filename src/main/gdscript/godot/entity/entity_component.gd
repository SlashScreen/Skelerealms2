@abstract
class_name EntityComponentGD
extends Node


var instance: EntityComponentPrototypeGD

@abstract func get_save_data_key() -> StringName

@abstract func get_save_data() -> Dictionary

@abstract func load_from_data(userdata: Dictionary) -> void
