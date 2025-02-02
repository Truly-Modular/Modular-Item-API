package smartin.miapi.material.base;

import com.google.gson.JsonElement;
import smartin.miapi.material.MaterialProperties;
import smartin.miapi.modules.ModuleDataPropertiesManager;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface PropertyController {
    /**
     * Retuns all Material Properties for this key, see {@link MaterialProperties} for more details
     *
     * @param key
     * @return
     */
    Map<ModuleProperty<?>, Object> materialProperties(String key);

    /**
     * be sure to also implement {@link Material#getAllDisplayPropertyKeys()}
     * if you implement this
     *
     * @param key
     * @return
     */
    default Map<ModuleProperty<?>, Object> getDisplayMaterialProperties(String key) {
        return materialProperties(key);
    }

    /**
     * retuns all unique property keys this Material has Properties for.
     *
     * @return
     */
    List<String> getAllPropertyKeys();

    /**
     * the property keys to be displayed in the UI
     *
     * @return
     */
    default List<String> getAllDisplayPropertyKeys() {
        return getAllPropertyKeys();
    }

    default Map<String, Map<ModuleProperty<?>, Object>> getHiddenProperty() {
        Map<String, Map<ModuleProperty<?>, Object>> propertyMap = new HashMap<>();
        getAllPropertyKeys().forEach(s -> {
            var blackList = getDisplayMaterialProperties(s);
            var properties = materialProperties(s);
            Map<ModuleProperty<?>, Object> map = new HashMap<>();
            for (ModuleProperty<?> property : properties.keySet()) {
                if (!blackList.containsKey(property)) {
                    map.put(property, properties.get(property));
                }
            }
            propertyMap.put(s, map);
        });
        return propertyMap;
    }

    default Map<String, Map<ModuleProperty<?>, Object>> getDisplayProperty() {
        Map<String, Map<ModuleProperty<?>, Object>> propertyMap = new HashMap<>();
        getAllDisplayPropertyKeys().forEach(s -> {
            propertyMap.put(s, getDisplayMaterialProperties(s));
        });
        return propertyMap;
    }

    default Map<String, Map<ModuleProperty<?>, Object>> getActualProperty() {
        Map<String, Map<ModuleProperty<?>, Object>> propertyMap = new HashMap<>();
        getAllPropertyKeys().forEach(s -> {
            propertyMap.put(s, materialProperties(s));
        });
        return propertyMap;
    }

    static Map<String, JsonElement> toJsonMap(Map<String, Map<ModuleProperty<?>, Object>> original) {
        Map<String, JsonElement> encoded = new HashMap<>();
        original.forEach((key, property) -> {
            encoded.put(key, ModuleDataPropertiesManager.createJsonFromProperties(property));
        });
        return encoded;
    }
}
