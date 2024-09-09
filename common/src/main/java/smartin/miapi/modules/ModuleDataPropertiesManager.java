package smartin.miapi.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.HashMap;
import java.util.Map;

/**
 * @header Data Types
 * @description_start
 * Data Types are commonly used Types of json encoded data.
 * Different systems all use these same types, so this is an explanation for the individual types
 * @desciption_end
 * @keywords Data Types, datatypes, data_types
 * @path /data_types
 */
public class ModuleDataPropertiesManager {

    public static Map<ModuleProperty<?>, Object> getProperties(ModuleInstance moduleInstance) {
        Map<ModuleProperty<?>, Object> map = new HashMap<>();
        JsonElement properties = moduleInstance.moduleData.get("properties");
        if (properties != null) {
            JsonObject moduleJson = properties.getAsJsonObject();
            if (moduleJson != null) {
                moduleJson.entrySet().forEach(stringJsonElementEntry -> {
                    ModuleProperty<?> property = RegistryInventory.moduleProperties
                            .get(Miapi.id(stringJsonElementEntry.getKey()));
                    if (property != null) {
                        map.put(property, property.decode(stringJsonElementEntry.getValue()));
                    }
                });
            }
        }
        return map;
    }

    public static <T> void setProperty(ModuleInstance moduleInstance, ModuleProperty<T> property, T propertyData) {
        Map<ModuleProperty<?>, Object> map = getProperties(moduleInstance);
        map.put(property, propertyData);
        setProperties(moduleInstance, map);
    }

    public static void setProperties(ModuleInstance moduleInstance, Map<ModuleProperty<?>, Object> propertyMap) {
        JsonObject object = new JsonObject();
        propertyMap.forEach(((moduleProperty, element) -> {
            ResourceLocation key = RegistryInventory.moduleProperties.findKey(moduleProperty);
            assert key != null;
            JsonElement encoded = encode(moduleProperty, element);
            if (encoded.isJsonPrimitive()) {
                object.add(key.toString(), encoded);
            } else {
                object.add(key.toString(), encoded);
            }
            //object.add(key, encode(moduleProperty, element));
        }));
        moduleInstance.moduleData.put("properties", object);
    }

    @SuppressWarnings("unchecked")
    private static <T> JsonElement encode(ModuleProperty<T> property, Object data) {
        return property.encode((T) data);
    }
}
