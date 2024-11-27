package smartin.miapi.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.HashMap;
import java.util.Map;

/**
 * @header Data Types
 * @description_start Data Types are commonly used Types of json encoded data.
 * Different systems all use these same types, so this is an explanation for the individual types
 * @desciption_end
 * @keywords Data Types, datatypes, data_types
 * @path /data_types
 */
public class ModuleDataPropertiesManager {

    public static Map<ModuleProperty<?>, Object> getProperties(ModuleInstance moduleInstance) {
        try {
            JsonElement properties = moduleInstance.moduleData.get(Miapi.id("properties"));
            return resolvePropertiesFromJson(properties);
        } catch (RuntimeException e) {
            Miapi.LOGGER.warn("could not decode properties", e);
        }
        return new HashMap<>();
    }

    public static Map<ModuleProperty<?>, Object> resolvePropertiesFromJson(JsonElement properties) {
        Map<ModuleProperty<?>, Object> map = new HashMap<>();
        if (properties != null) {
            JsonObject moduleJson = properties.getAsJsonObject();
            if (moduleJson != null) {
                moduleJson.entrySet().forEach(stringJsonElementEntry -> {
                    ModuleProperty<?> property = RegistryInventory.moduleProperties
                            .get(Miapi.id(stringJsonElementEntry.getKey()));
                    if (property != null) {
                        try {
                            if (property.load(Miapi.id("runtime_data"), stringJsonElementEntry.getValue(), Environment.isClient())) {
                                map.put(property, property.decode(stringJsonElementEntry.getValue()));
                            }
                        } catch (Exception e) {
                            Miapi.LOGGER.info("could not load property " + stringJsonElementEntry.getKey());
                        }
                    }
                });
            }
        }
        return map;
    }

    public static @NotNull JsonObject createJsonFromProperties(Map<ModuleProperty<?>, Object> propertyMap) {
        JsonObject object = new JsonObject();
        propertyMap.forEach(((moduleProperty, element) -> {
            ResourceLocation key = RegistryInventory.moduleProperties.findKey(moduleProperty);
            assert key != null;
            try {
                JsonElement encoded = encode(moduleProperty, element);
                if (encoded != null) {
                    if (encoded.isJsonPrimitive()) {
                        object.add(key.toString(), encoded);
                    } else {
                        object.add(key.toString(), encoded);
                    }
                } else {
                    Miapi.LOGGER.error("could not encode property " + key);
                }
            } catch (RuntimeException e) {
                Miapi.LOGGER.error("could not encode property " + key, e);
            }
        }));
        return object;
    }

    public static <T> void setProperty(ModuleInstance moduleInstance, ModuleProperty<T> property, @Nullable T propertyData) {
        Map<ModuleProperty<?>, Object> map = getProperties(moduleInstance);
        if (propertyData == null) {
            map.remove(property);
        } else {
            map.put(property, propertyData);
        }
        setProperties(moduleInstance, map);
    }

    public static void setProperties(ModuleInstance moduleInstance, Map<ModuleProperty<?>, Object> propertyMap) {
        JsonObject object = createJsonFromProperties(propertyMap);
        moduleInstance.moduleData.put(Miapi.id("properties"), object);
    }

    @SuppressWarnings("unchecked")
    private static <T> JsonElement encode(ModuleProperty<T> property, Object data) {
        return property.encode((T) data);
    }
}
