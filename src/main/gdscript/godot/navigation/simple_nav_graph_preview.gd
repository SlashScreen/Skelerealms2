@tool
class_name SimpleNavGraphPreview
extends Node3D


@export var graph: SimpleNavGraph
@export var draw: DebugDraw3D

var _lines: PackedVector3Array

func _ready() -> void:
	if not Engine.is_editor_hint():
		if draw: # if we have a debug draw, we get its connections
			# this will double up, but I don't super care rn
			var astar := graph.into_a_star()
			for pt: int in astar.get_point_ids():
				var src: Vector3 = astar.get_point_position(pt)
				for to: int in astar.get_point_connections(pt):
					_lines.append(src)
					_lines.append(astar.get_point_position(to))
		else:
			queue_free()


func _process(_delta: float) -> void:
	if draw:
		draw.draw_line_list(_lines, Color.BLUE_VIOLET)
