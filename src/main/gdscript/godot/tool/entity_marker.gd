@tool
class_name EntityMarker
extends Marker3D

@export var instance: EntityInstance


func _process(_delta: float) -> void:
	if Engine.is_editor_hint():
		#instance.transform = global_transform
		pass
	else:
		queue_free()
