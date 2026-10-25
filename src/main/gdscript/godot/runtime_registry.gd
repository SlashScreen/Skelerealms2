@tool
class_name RuntimeRegistry
extends Resource


## The directory this gets resources from.
@export_dir var scan_path: String

## All names this registry has.
var names: Array[StringName]:
	get: return _names_to_ids.keys()
## All UIDs this registry has.
var ids: Array[int]:
	get: return _names_to_ids.values()

## Name to ID table.
var _names_to_ids: Dictionary[StringName, int]


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
			var uid := ResourceUID.text_to_id(ResourceUID.path_to_uid(res_path))
			_names_to_ids[StringName(res_name)] = uid


## Load an entry from name, with optional hint string and cache mode. Returns null if none found.
func load_entry(name: StringName, type_hint: String = "", cache_mode: ResourceLoader.CacheMode = ResourceLoader.CacheMode.CACHE_MODE_REUSE) -> Resource:
	if not _names_to_ids.has(name): return null

	var path := ResourceUID.uid_to_path(ResourceUID.id_to_text(_names_to_ids[name]))
	return ResourceLoader.load(path, type_hint, cache_mode)


## Load all entries, blocking.
func load_all_blocking(type_hint: String = "", cache_mode: ResourceLoader.CacheMode = ResourceLoader.CacheMode.CACHE_MODE_REUSE) -> Dictionary[StringName, Resource]:
	var res: Dictionary[StringName, Resource] = {}

	for name: StringName in _names_to_ids:
		res[name] = ResourceLoader.load(ResourceUID.uid_to_path(ResourceUID.id_to_text(_names_to_ids[name])), type_hint, cache_mode)

	return res


## Check if this registry has an entry.
func has_entry(name: StringName) -> bool:
	return _names_to_ids.has(name)


## Get UID for an entry. -1 if none exists.
func get_id_for_entry(name: StringName) -> int:
	return _names_to_ids[name] if has_entry(name) else -1
