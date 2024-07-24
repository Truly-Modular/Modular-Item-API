package smartin.miapi.modules.synergies;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
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
        PropertyResolver.register("synergies", (moduleInstance, oldMap) -> {
            if (moduleInstance != null) {
                List<Synergy> synergies = maps.get(moduleInstance.module);
                if (synergies != null) {
                    synergies.forEach(synergy -> {
                        if (synergy.condition.isAllowed(ConditionManager.moduleContext(moduleInstance, oldMap))) {
                            synergy.holder.applyHolder(oldMap);
                        }
                    });
                }
            }
            return oldMap;
        });
        Miapi.registerReloadHandler(ReloadEvents.MAIN, "miapi/synergies", maps, (isClient, path, data) -> {
            load(data, path);
        }, 2);
        ReloadEvents.END.subscribe((isClient -> {
            int size = 0;
            for (List<Synergy> synergies : maps.values()) {
                size += synergies.size();
            }
            Miapi.LOGGER.info("Loaded " + size + " Synergies");
        }));
    }

    public static void load(String data, ResourceLocation path) {
        JsonObject element = Miapi.gson.fromJson(data, JsonObject.class);
        element.getAsJsonObject().entrySet().forEach((entry) -> {
            if (element.has("type")) {
                String type = element.get("type").getAsString();
                if (type.equals("tag")) {
                    String tagKey = entry.getKey();
                    TagProperty.getModulesWithTag(tagKey).forEach(itemModule -> {
                        loadSynergy(itemModule, entry.getValue().getAsJsonObject(), path);
                    });
                }
                if (type.equals("material")) {
                    String tagKey = entry.getKey();
                    Material material = MaterialProperty.materials.get(tagKey);
                    if (material != null) {
                        loadSynergy(material, entry.getValue().getAsJsonObject(), path);
                    }
                }
                if (type.equals("all")) {
                    if (entry.getValue().isJsonObject()) {
                        RegistryInventory.modules.getFlatMap().forEach((id, module) -> {
                            loadSynergy(module, entry.getValue().getAsJsonObject(), path);
                        });
                    }
                }
            } else {
                ItemModule property = RegistryInventory.modules.get(entry.getKey());
                JsonObject entryData = entry.getValue().getAsJsonObject();
                loadSynergy(property, entryData, path);
            }
        });
    }

    public static void loadSynergy(ItemModule itemModule, JsonObject entryData, ResourceLocation id) {
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
        synergy.id = id;
        synergy.holder = PropertyHolderJsonAdapter.readFromObject(entryData, Environment.isClient(), synergy.id);
    }

    public static void loadSynergy(Material material, JsonObject entryData, ResourceLocation id) {
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
        synergy.id = id;
        synergy.holder = PropertyHolderJsonAdapter.readFromObject(entryData, Environment.isClient(), synergy.id);
    }

    public static PropertyHolder getFrom(JsonElement element, boolean isClient, ResourceLocation context) {
        return PropertyHolderJsonAdapter.readFromObject(element, isClient, context);
    }

    public static class Synergy {
        public ModuleCondition condition;
        public PropertyHolder holder = new PropertyHolder();
        public ResourceLocation id;
    }

    public static Map<ModuleProperty<?>, Object> getProperties(@Nullable JsonElement element, boolean isClient, ResourceLocation source, String context) {
        Map<ModuleProperty<?>, Object> properties = new HashMap<>();
        if (element == null || element.isJsonNull() || element.isJsonPrimitive()) {
            return properties;
        }
        element.getAsJsonObject().entrySet().forEach(propertyEntry -> {
            String propertyKey = propertyEntry.getKey();
            ModuleProperty<?> property = RegistryInventory.moduleProperties.get(propertyKey);
            if (property != null) {
                try {
                    if (property.load(source, propertyEntry.getValue(), isClient)) {
                        properties.put(property, property.decode(propertyEntry.getValue()));
                    }
                } catch (Exception e) {
                    Miapi.LOGGER.error("could not load property " + propertyKey + " in context " + context + " from source " + source.toString(), e);
                    Miapi.LOGGER.error(Miapi.gson.toJson(element));
                }
            } else {
                Miapi.LOGGER.warn("could not find property " + propertyKey + " in context " + context + " from source " + source.toString());
                Miapi.LOGGER.error(Miapi.gson.toJson(element));
            }
        });
        return properties;
    }

    public static List<ModuleProperty<?>> getRemoveProperties(JsonElement element, ResourceLocation source, String context) {
        List<ModuleProperty<?>> removeFields = new ArrayList<>();
        if (element == null) {
            return removeFields;
        }
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach((element1 -> {
                if (element1.isJsonPrimitive()) {
                    String key = element1.getAsString();
                    ModuleProperty<?> moduleProperty = RegistryInventory.moduleProperties.get(key);
                    if (moduleProperty != null) {
                        removeFields.add(moduleProperty);
                    } else {
                        Miapi.LOGGER.error("Could not find Property " + key + " in context " + context + " from source " + source.toString());
                        Miapi.LOGGER.error(Miapi.gson.toJson(element));
                    }
                }
            }));
        }
        return removeFields;
    }

    @JsonAdapter(PropertyHolderJsonAdapter.class)
    public static class PropertyHolder {
        public Map<ModuleProperty<?>, Object> replace = new HashMap<>();
        public Map<ModuleProperty<?>, Object> merge = new HashMap<>();
        public List<ModuleProperty<?>> remove = new ArrayList<>();

        public Map<ModuleProperty<?>, Object> applyHolder(Map<ModuleProperty<?>, Object> oldMap) {
            remove.forEach((oldMap::remove));
            merge.forEach((key, value) -> {
                if (oldMap.containsKey(key)) {
                    oldMap.put(key, ItemModule.merge(key, oldMap.get(key), value, MergeType.SMART));
                } else {
                    oldMap.put(key, value);
                }
            });
            oldMap.putAll(replace);
            return oldMap;
        }

        public Map<ModuleProperty<?>, Object> applyHolderRaw(Map<ModuleProperty<?>, Object> oldMap) {
            remove.forEach(oldMap::remove);
            merge.forEach((key, value) -> {
                if (oldMap.containsKey(key)) {
                    oldMap.put(key, ItemModule.merge(key, oldMap.get(key), value, MergeType.SMART));
                } else {
                    oldMap.put(key, value);
                }
            });
            oldMap.putAll(replace);
            return oldMap;
        }
    }

    public static class PropertyHolderJsonAdapter extends TypeAdapter<PropertyHolder> {

        @Override
        public void write(JsonWriter jsonWriter, PropertyHolder propertyHolder) throws IOException {

        }

        @Override
        public PropertyHolder read(JsonReader jsonReader) throws IOException {
            return readFromObject(Miapi.gson.fromJson(jsonReader, JsonElement.class), Environment.isClient(), Miapi.id("miapi:runtime_property_holder"));
        }

        public static PropertyHolder readFromObject(JsonElement jsonElement, boolean isClient, ResourceLocation source) {
            JsonObject entryData = jsonElement.getAsJsonObject();
            PropertyHolder propertyHolder = new PropertyHolder();
            JsonElement replaceProperty = entryData.get("replace");
            if (entryData.has("properties")) {
                replaceProperty = entryData.get("properties");
                Miapi.LOGGER.warn("The raw use of the Field `properties` should be replaced with the field `replace` in " + source);
            }
            propertyHolder.replace = getProperties(replaceProperty, isClient, source, "replace");
            propertyHolder.merge = getProperties(entryData.get("merge"), isClient, source, "merge");
            propertyHolder.remove = getRemoveProperties(entryData.get("remove"), source, "remove");
            return propertyHolder;
        }
    }
}
