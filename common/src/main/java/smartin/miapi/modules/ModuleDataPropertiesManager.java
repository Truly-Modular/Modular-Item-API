package smartin.miapi.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.HashMap;
import java.util.Map;

public class ModuleDataPropertiesManager {

    public static Map<ModuleProperty<?>, JsonElement> getProperties(ModuleInstance moduleInstance) {
        Map<ModuleProperty<?>, JsonElement> map = new HashMap<>();
        String properties = moduleInstance.moduleData.get("properties");
        if (properties != null) {
            JsonObject moduleJson = Miapi.gson.fromJson(properties, JsonObject.class);
            if (moduleJson != null) {
                moduleJson.entrySet().forEach(stringJsonElementEntry -> {
                    ModuleProperty<?> property = RegistryInventory.moduleProperties
                            .get(stringJsonElementEntry.getKey());
                    if (property != null) {
                        map.put(property, stringJsonElementEntry.getValue());
                    }
                });
            }
        }
        return map;
    }

    public static void setProperties(ModuleInstance moduleInstance, Map<ModuleProperty<?>, JsonElement> propertyMap) {
        JsonObject object = new JsonObject();
        propertyMap.forEach(((moduleProperty, element) -> {
            String key = RegistryInventory.moduleProperties.findKey(moduleProperty);
            assert key != null;
            object.add(key, element);
        }));
        moduleInstance.moduleData.put("properties", Miapi.gson.toJson(object));
    }
}
