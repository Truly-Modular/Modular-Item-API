package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.material.AllowedMaterial;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.item.ItemStack;

public class ModuleStats implements ModuleProperty {
    public static String KEY = "module_stats";
    public static ModuleStats property;

    public ModuleStats() {
        property = this;
        ModularItemCache.setSupplier(KEY, ModuleStats::createCache);
        StatResolver.registerResolver("module", new StatResolver.Resolver() {
            @Override
            public double resolveDouble(String data, ModuleInstance instance) {
                if(instance.module.equals(ItemModule.internal)){
                    return 1.0;
                }
                if ("cost".equals(data)) {
                    return AllowedMaterial.getMaterialCost(instance);
                }
                return getStats(instance).getOrDefault(data, 0);
            }

            @Override
            public String resolveString(String data, ModuleInstance instance) {
                return null;
            }
        });
    }

    public static Map<String, Integer> getStats(ItemStack itemStack) {
        return ModularItemCache.get(itemStack, KEY, new HashMap<>());
    }

    public static Map<String, Integer> getStats(ModuleInstance moduleInstance) {
        JsonElement element = property.getJsonElement(moduleInstance);
        if (element != null) {
            return getStats(element);
        }
        return new HashMap<>();
    }

    private static Map<String, Integer> createCache(ItemStack itemStack) {
        JsonElement element = property.getJsonElement(itemStack);
        if (element != null) {
            return getStats(element);
        }
        return new HashMap<>();

    }

    public static Map<String, Integer> getStats(JsonElement element) {
        Map<String, Integer> map = new HashMap<>();

        if (element != null && element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();

            for (Map.Entry<String, JsonElement> entry : entrySet) {
                String key = entry.getKey();
                JsonElement valueElement = entry.getValue();

                if (valueElement.isJsonPrimitive() && ((JsonPrimitive) valueElement).isNumber()) {
                    int value = valueElement.getAsInt();
                    map.put(key, value);
                } else {
                    // Handle non-integer values if needed
                    throw new JsonParseException("Value for key '" + key + "' is not an integer.");
                }
            }
        }

        return map;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        getStats(data);
        return true;
    }
}
