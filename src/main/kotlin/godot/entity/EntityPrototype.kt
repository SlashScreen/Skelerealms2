package godot.entity

import godot.annotation.HintString
import godot.annotation.Script
import godot.api.Resource
import godot.core.VariantArray

@Script
class EntityPrototype: Resource() {
    @HintString("24/17:EntityComponentInstance,EntityComponentInstanceGD")
    var components: VariantArray<Resource> = VariantArray()
}
