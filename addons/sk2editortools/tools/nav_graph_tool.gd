@tool
extends EditorInspectorPlugin


const OUTPUT := "res://navgraphs/" # TODO: Not hardcode


func _can_handle(object: Object) -> bool:
	return object is NavigationMesh


func _parse_end(object: Object) -> void:
	var nv := object as NavigationMesh

	var button := Button.new()
	button.text = "Bake simple graph"
	button.pressed.connect(_bake.bind(nv))

	add_custom_control(button)


func _bake(mesh: NavigationMesh) -> void:
	print("Bake pressed")
	var baker := SimpleNavGraphBuilder.new()
	var graph := baker.build_simple_nav_graph(mesh)

	var scene_name := EditorInterface.get_edited_scene_root().name
	ResourceSaver.save(graph, OUTPUT + "%s.res" % scene_name)
