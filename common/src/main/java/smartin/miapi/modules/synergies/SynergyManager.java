package smartin.miapi.modules.synergies;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.conditions.ModuleCondition;
import smartin.miapi.modules.conditions.TrueCondition;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.MiapiRegistry;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SynergyManager {
    public static Map<ItemModule, List<Synergy>> maps = new HashMap<>();

    public static void setup() {
        PropertyResolver.propertyProviderRegistry.register("synergies", (moduleInstance, oldMap) -> {
            if (moduleInstance != null) {
                maps.forEach((itemModule, synergies) -> {
                    if (moduleInstance.module.equals(itemModule)) {
                        synergies.forEach(synergy -> {
                            if (synergy.condition.isAllowed(moduleInstance, oldMap)) {
                                oldMap.putAll(synergy.properties);
                            }
                        });
                    }
                });
            }
            return oldMap;
        });
        ReloadEvents.END.subscribe((isClient -> {
            ReloadEvents.DATA_PACKS.forEach((path, data) -> {
                if (path.startsWith("synergies")) {
                    load(data);
                }
            });
        }));
    }

    public static void load(String data) {
        JsonObject element = Miapi.gson.fromJson(data, JsonObject.class);
        element.getAsJsonObject().entrySet().forEach((entry) -> {
            ItemModule property = RegistryInventory.modules.get(entry.getKey());
            JsonObject entryData = entry.getValue().getAsJsonObject();
            Synergy synergy = new Synergy();
            synergy.condition = ConditionManager.get(entryData.get("condition"));
            List<Synergy> synergies = maps.computeIfAbsent(property, (module) -> {
                return new ArrayList<>();
            });
            synergies.add(synergy);
            JsonObject object = entry.getValue().getAsJsonObject().get("properties").getAsJsonObject();
            object.entrySet().forEach(propertyEntry -> {
                ModuleProperty property1 = RegistryInventory.moduleProperties.get(propertyEntry.getKey());
                try {
                    property1.load("synergy", propertyEntry.getValue());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                synergy.properties.put(property1, propertyEntry.getValue());
            });
        });
    }

    public static Map<ModuleProperty, JsonElement> getProperties(JsonElement element) {
        Map<ModuleProperty, JsonElement> properties = new HashMap<>();
        element.getAsJsonObject().entrySet().forEach(propertyEntry -> {
            ModuleProperty property1 = RegistryInventory.moduleProperties.get(propertyEntry.getKey());
            try {
                property1.load("synergy", propertyEntry.getValue());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            properties.put(property1, propertyEntry.getValue());
        });
        return properties;
    }

    public static class Synergy {
        public ModuleCondition condition;
        public Map<ModuleProperty, JsonElement> properties = new HashMap<>();
    }

}
