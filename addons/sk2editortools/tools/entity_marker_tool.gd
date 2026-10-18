@tool
extends EditorNode3DGizmoPlugin

var _sphere: Mesh = SphereMesh.new()


func _init() -> void:
	create_material("main", Color(1, 0, 0))


func _get_gizmo_name():
	return "EntityMarker"


func _has_gizmo(for_node_3d: Node3D) -> bool:
	return for_node_3d is EntityMarker


func _redraw(gizmo: EditorNode3DGizmo) -> void:
	print("Draw")
	gizmo.clear()

	gizmo.add_mesh(_sphere, get_material("main", gizmo))

	var n := gizmo.get_node_3d() as EntityMarker
	for c: Node in n.get_children():
		c.queue_free()

	n.instance.changed.connect(
		func() -> void:
			print("Changed"),
		ConnectFlags.CONNECT_ONE_SHOT,
	)
	if not n.instance == null:
		for c: Resource in n.instance.prototype.components:
			if c is PuppetComponentPrototype:
				var pupp := (c as PuppetComponentPrototype).puppet_scene
				if pupp == null:
					return

				n.add_child(pupp.instantiate())

				break
