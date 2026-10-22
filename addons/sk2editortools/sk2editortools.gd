@tool
extends EditorPlugin

const EntityMarkerTool = preload("uid://dnkd156e10cpn")
const NavGraphTool = preload("uid://b4pp2kivaqhjh")

var _marker_plugin := EntityMarkerTool.new()
var _nav_graph_plugin := NavGraphTool.new()


func _enter_tree() -> void:
	add_node_3d_gizmo_plugin(_marker_plugin)
	add_inspector_plugin(_nav_graph_plugin)


func _exit_tree() -> void:
	remove_node_3d_gizmo_plugin(_marker_plugin)
	remove_inspector_plugin(_nav_graph_plugin)
