@tool
class_name RuntimeRegistry
extends Resource


## The directory this gets resources from.
@export_dir var scan_path: String

## All names this registry has.
var names: Array[StringName]:
	get: return _names_to_paths.keys()
## All paths this registry has.
var paths: Array[String]:
	get: return _names_to_paths.values()

## Name to path table.May be res:// or uid://
var _names_to_paths: Dictionary[StringName, String]


## Scan a directory and catalogue all within.
func scan(path: String = scan_path) -> void:
	var scan_dir := path if path.ends_with("/") else path + "/"
	var items := ResourceLoader.list_directory(scan_dir)

	for item: String in items:
		var res_path := scan_dir + item

		if res_path.ends_with("/"): # dir
			scan(res_path)
		else: #file
			var res_name := item.split(".")[0]
			_names_to_paths[StringName(res_name)] = ResourceUID.path_to_uid(res_path)


## Load an entry from name, with optional hint string and cache mode. Returns null if none found.
func load_entry(name: StringName, type_hint: String = "", cache_mode: ResourceLoader.CacheMode = ResourceLoader.CacheMode.CACHE_MODE_REUSE) -> Resource:
	if not _names_to_paths.has(name): return null

	var path := ResourceUID.ensure_path(_names_to_paths[name])
	return ResourceLoader.load(path, type_hint, cache_mode)


## Load all entries, blocking.
func load_all_blocking(type_hint: String = "", cache_mode: ResourceLoader.CacheMode = ResourceLoader.CacheMode.CACHE_MODE_REUSE) -> Dictionary[StringName, Resource]:
	var res: Dictionary[StringName, Resource] = {}

	for name: StringName in _names_to_paths:
		res[name] = ResourceLoader.load(ResourceUID.ensure_path(_names_to_paths[name]), type_hint, cache_mode)

	return res


## Check if this registry has an entry.
func has_entry(name: StringName) -> bool:
	return _names_to_paths.has(name)


## Get path for an entry. EMpty if none exists.
func get_path_for_entry(name: StringName) -> String:
	return _names_to_paths[name] if has_entry(name) else ""
