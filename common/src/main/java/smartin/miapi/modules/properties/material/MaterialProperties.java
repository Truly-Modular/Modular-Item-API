package smartin.miapi.modules.properties.material;

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
        PropertyResolver.propertyProviderRegistry.register(KEY, (moduleInstance, oldMap) -> {
            Material material = MaterialProperty.getMaterial(oldMap.get(MaterialProperty.property));
            Map<ModuleProperty, JsonElement> returnMap = new HashMap<>(oldMap);
            if (material != null && oldMap.containsKey(property)) {
                List<String> keys = new ArrayList<>();
                for (JsonElement element : oldMap.get(property).getAsJsonArray()) {
                    keys.add(element.getAsString());
                    returnMap = PropertyResolver.merge(oldMap, material.materialProperties(element.getAsString()), MergeType.SMART);
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
