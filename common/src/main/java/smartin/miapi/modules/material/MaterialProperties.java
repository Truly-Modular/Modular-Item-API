package smartin.miapi.modules.material;

import com.google.gson.JsonElement;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This property allows materials to set specific Properties
 */
public class MaterialProperties implements ModuleProperty {
    public static String KEY = "materialProperty";
    public static MaterialProperties property;

    public MaterialProperties() {
        property = this;
        PropertyResolver.register("material_property", (moduleInstance, oldMap) -> {
            Material material = MaterialProperty.getMaterial(oldMap.get(MaterialProperty.property));
            Map<ModuleProperty, JsonElement> returnMap = new HashMap<>(oldMap);
            if (material != null) {
                List<String> keys = new ArrayList<>();
                if (oldMap.containsKey(property)) {
                    for (JsonElement element : oldMap.get(property).getAsJsonArray()) {
                        keys.add(element.getAsString());
                    }
                }
                if (keys.isEmpty()) {
                    keys.add("default");
                }
                if (moduleInstance.module != null) {
                    keys.add(moduleInstance.module.name());
                }
                for (String key : keys) {
                    Map<ModuleProperty, JsonElement> materialProperties = material.materialProperties(key);
                    if (!materialProperties.isEmpty()) {
                        returnMap = PropertyResolver.merge(oldMap, materialProperties, MergeType.SMART);
                    }
                }
            }
            return returnMap;
        });
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        return ModuleProperty.super.merge(old, toMerge, type);
    }
}
