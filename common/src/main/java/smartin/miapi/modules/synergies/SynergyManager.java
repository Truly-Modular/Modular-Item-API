package smartin.miapi.modules.synergies;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.text.Text;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.conditions.ModuleCondition;
import smartin.miapi.modules.properties.TagProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SynergyManager {
    public static Map<ItemModule, List<Synergy>> maps = new ConcurrentHashMap<>();

    public static void setup() {
        PropertyResolver.propertyProviderRegistry.register("synergies", (moduleInstance, oldMap) -> {
            if (moduleInstance != null) {
                List<Synergy> synergies = maps.get(moduleInstance.module);
                if (synergies != null) {
                    synergies.forEach(synergy -> {
                        List<Text> error = new ArrayList<>();
                        if (synergy.condition.isAllowed(new ConditionManager.ModuleConditionContext(moduleInstance, null, null, oldMap, error))) {
                            oldMap.putAll(synergy.properties);
                        }
                    });
                }
            }
            return oldMap;
        });
        Miapi.registerReloadHandler(ReloadEvents.MAIN, "synergies", maps, (isClient, path, data) -> {
            load(data);
        }, 2);
        ReloadEvents.END.subscribe((isClient -> {
            int size = 0;
            for (List<Synergy> synergies : maps.values()) {
                size += synergies.size();
            }
            Miapi.LOGGER.info("Loaded " + size + " Synergies");
        }));
    }

    public static void load(String data) {
        JsonObject element = Miapi.gson.fromJson(data, JsonObject.class);
        element.getAsJsonObject().entrySet().forEach((entry) -> {
            if (element.has("type")) {
                String type = element.get("type").getAsString();
                if (type.equals("tag")) {
                    String tagKey = entry.getKey();
                    TagProperty.getModulesWithTag(tagKey).forEach(itemModule -> {
                        loadSynergy(itemModule, entry.getValue().getAsJsonObject());
                    });
                }
            } else {
                ItemModule property = RegistryInventory.modules.get(entry.getKey());
                JsonObject entryData = entry.getValue().getAsJsonObject();
                loadSynergy(property, entryData);
            }
        });
    }

    public static void loadSynergy(ItemModule itemModule, JsonObject entryData) {
        if (itemModule == null) {
            Miapi.LOGGER.warn("ItemModule is null?");
            return;
        }
        Synergy synergy = new Synergy();
        synergy.condition = ConditionManager.get(entryData.get("condition"));
        List<Synergy> synergies = maps.computeIfAbsent(itemModule, (module) -> {
            return new ArrayList<>();
        });
        synergies.add(synergy);
        JsonObject object = entryData.get("properties").getAsJsonObject();
        object.entrySet().forEach(propertyEntry -> {
            ModuleProperty property1 = RegistryInventory.moduleProperties.get(propertyEntry.getKey());
            try {
                assert property1 != null;
                property1.load("synergy", propertyEntry.getValue());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            synergy.properties.put(property1, propertyEntry.getValue());
        });
    }

    public static Map<ModuleProperty, JsonElement> getProperties(JsonElement element) {
        Map<ModuleProperty, JsonElement> properties = new HashMap<>();
        element.getAsJsonObject().entrySet().forEach(propertyEntry -> {
            ModuleProperty property1 = RegistryInventory.moduleProperties.get(propertyEntry.getKey());
            try {
                assert property1 != null;
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
