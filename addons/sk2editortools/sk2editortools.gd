@tool
extends EditorPlugin

const EntityMarkerTool = preload("uid://dnkd156e10cpn")

var _marker_plugin := EntityMarkerTool.new()


func _enter_tree() -> void:
	add_node_3d_gizmo_plugin(_marker_plugin)


func _exit_tree() -> void:
	remove_node_3d_gizmo_plugin(_marker_plugin)
