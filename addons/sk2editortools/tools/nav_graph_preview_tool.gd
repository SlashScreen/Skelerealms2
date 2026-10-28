@tool
extends EditorNode3DGizmoPlugin


func _init() -> void:
	create_material("main", Color(1, 0, 0))
	create_material("astar", Color(1, 0.5, 0))


func _get_gizmo_name():
	return "NavGraphPreview"


func _has_gizmo(for_node_3d: Node3D) -> bool:
	return for_node_3d is SimpleNavGraphPreview


func _redraw(gizmo: EditorNode3DGizmo) -> void:
	var preview := gizmo.get_node_3d() as SimpleNavGraphPreview
	var graph := preview.graph
	if graph == null:
		return

	gizmo.add_lines(
		Array(graph.connections).map(
			func(i: int) -> Vector3:
				return graph.points[i],
		),
		get_material("main", gizmo),
		true
	)
