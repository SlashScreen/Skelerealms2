@tool
class_name SimpleNavGraphPreview
extends Node3D


@export var graph: SimpleNavGraph


func _ready() -> void:
	if not Engine.is_editor_hint():
		queue_free()
