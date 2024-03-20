package smartin.miapi.modules.synergies;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.text.Text;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.conditions.ModuleCondition;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.TagProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SynergyManager {
    public static Map<ItemModule, List<Synergy>> maps = new ConcurrentHashMap<>();
    public static Map<Material, List<Synergy>> materialSynergies = new ConcurrentHashMap<>();

    public static void setup() {
        PropertyResolver.propertyProviderRegistry.register("synergies", (moduleInstance, oldMap) -> {
            if (moduleInstance != null) {
                List<Synergy> synergies = maps.get(moduleInstance.module);
                if (synergies != null) {
                    synergies.forEach(synergy -> {
                        List<Text> error = new ArrayList<>();
                        if (synergy.condition.isAllowed(new ConditionManager.ModuleConditionContext(moduleInstance, null, null, oldMap, error))) {
                            synergy.holder.applyHolder(oldMap);
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
                if (type.equals("material")) {
                    String tagKey = entry.getKey();
                    Material material = MaterialProperty.materials.get(tagKey);
                    if (material != null) {
                        loadSynergy(material, entry.getValue().getAsJsonObject());
                    }
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
        synergy.holder = PropertyHolderJsonAdapter.readFromObject(entryData, "synergy" + entryData);
    }

    public static void loadSynergy(Material material, JsonObject entryData) {
        if (material == null) {
            Miapi.LOGGER.warn("ItemModule is null?");
            return;
        }
        Synergy synergy = new Synergy();
        synergy.condition = ConditionManager.get(entryData.get("condition"));
        List<Synergy> synergies = materialSynergies.computeIfAbsent(material, (module) -> {
            return new ArrayList<>();
        });
        synergies.add(synergy);
        synergy.holder = PropertyHolderJsonAdapter.readFromObject(entryData, "synergy" + entryData);
    }

    public static PropertyHolder getFrom(JsonElement element, String context) {
        return PropertyHolderJsonAdapter.readFromObject(element, context);
    }

    public static class Synergy {
        public ModuleCondition condition;
        public PropertyHolder holder = new PropertyHolder();
    }

    public static Map<ModuleProperty, JsonElement> getProperties(JsonElement element) {
        Map<ModuleProperty, JsonElement> properties = new HashMap<>();
        if (element == null) {
            return properties;
        }
        element.getAsJsonObject().entrySet().forEach(propertyEntry -> {
            ModuleProperty property1 = RegistryInventory.moduleProperties.get(propertyEntry.getKey());
            try {
                assert property1 != null;
                property1.load("synergy", propertyEntry.getValue(), Environment.isClient());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            properties.put(property1, propertyEntry.getValue());
        });
        return properties;
    }

    public static List<ModuleProperty> getRemoveProperties(JsonElement element) {
        List<ModuleProperty> removeFields = new ArrayList<>();
        if (element == null) {
            return removeFields;
        }
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach((element1 -> {
                if (element1.isJsonPrimitive()) {
                    String key = element1.getAsString();
                    ModuleProperty moduleProperty = RegistryInventory.moduleProperties.get(key);
                    if (moduleProperty != null) {
                        removeFields.add(moduleProperty);
                    }
                }
            }));
        }
        return removeFields;
    }

    @JsonAdapter(PropertyHolderJsonAdapter.class)
    public static class PropertyHolder {
        public Map<ModuleProperty, JsonElement> replace = new HashMap<>();
        public Map<ModuleProperty, JsonElement> merge = new HashMap<>();
        public List<ModuleProperty> remove = new ArrayList<>();

        public Map<ModuleProperty, JsonElement> applyHolder(Map<ModuleProperty, JsonElement> oldMap) {
            remove.forEach((oldMap::remove));
            merge.forEach((key, value) -> {
                if (oldMap.containsKey(key)) {
                    oldMap.put(key, key.merge(oldMap.get(key), value, MergeType.SMART));
                } else {
                    oldMap.put(key, value);
                }
            });
            oldMap.putAll(replace);
            return oldMap;
        }

        public Map<String, JsonElement> applyHolderRaw(Map<String, JsonElement> oldMap) {
            remove.forEach(key -> {
                String stringKey = RegistryInventory.moduleProperties.findKey(key);
                oldMap.remove(stringKey);
            });
            merge.forEach((key, value) -> {
                String stringKey = RegistryInventory.moduleProperties.findKey(key);
                if (oldMap.containsKey(stringKey)) {
                    oldMap.put(stringKey, key.merge(oldMap.get(stringKey), value, MergeType.SMART));
                } else {
                    oldMap.put(stringKey, value);
                }
            });
            replace.forEach((key, value) -> {
                String stringKey = RegistryInventory.moduleProperties.findKey(key);
                oldMap.put(stringKey, value);
            });
            return oldMap;
        }
    }

    public static class PropertyHolderJsonAdapter extends TypeAdapter<PropertyHolder> {

        @Override
        public void write(JsonWriter jsonWriter, PropertyHolder propertyHolder) throws IOException {

        }

        @Override
        public PropertyHolder read(JsonReader jsonReader) throws IOException {
            return readFromObject(Miapi.gson.fromJson(jsonReader, JsonElement.class), "context missing");
        }

        public static PropertyHolder readFromObject(JsonElement jsonElement, String context) {
            JsonObject entryData = jsonElement.getAsJsonObject();
            PropertyHolder propertyHolder = new PropertyHolder();
            JsonElement replaceProperty = entryData.get("replace");
            if (entryData.has("properties")) {
                replaceProperty = entryData.get("properties");
                Miapi.LOGGER.warn("The raw use of the Field `properties` should be replaced with the field `replace`");
                Miapi.LOGGER.warn(context);
            }
            propertyHolder.replace = getProperties(replaceProperty);
            propertyHolder.merge = getProperties(entryData.get("merge"));
            propertyHolder.remove = getRemoveProperties(entryData.get("remove"));
            return propertyHolder;
        }
    }
}
