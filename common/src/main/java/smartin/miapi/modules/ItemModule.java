package smartin.miapi.modules;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static smartin.miapi.Miapi.LOGGER;
import static smartin.miapi.Miapi.gson;

/**
 * An ItemModule represents a Module loaded from a JSON
 *
 * @param name       The name of the module.
 * @param properties The map of properties for the module.
 */
public record ItemModule(String name, Map<String, JsonElement> properties) {

    /**
     * The key for the properties in the Cache.
     */
    public static final String MODULE_KEY = "modules";
    /**
     *
     */
    public static final String NBT_MODULE_KEY = "miapi_modules";
    /**
     * The key for the raw properties in the Cache.
     */
    public static final String PROPERTY_KEY = "rawProperties";
    /**
     * An empty ItemModule instance.
     */
    public static final ItemModule empty = new ItemModule("empty", new HashMap<>());


    public Map<ModuleProperty, JsonElement> getKeyedProperties() {
        HashMap<ModuleProperty, JsonElement> map = new HashMap<>();
        properties().forEach((key, jsonData) -> map.put(RegistryInventory.moduleProperties.get(key), jsonData));
        return map;
    }

    /**
     * Loads an ItemModule from a JSON string.
     *
     * @param path             the path of the JSON file
     * @param moduleJsonString the JSON string to load from
     */
    public static void loadFromData(String path, String moduleJsonString, boolean isClient) {
        try {
            JsonObject moduleJson = gson.fromJson(moduleJsonString, JsonObject.class);
            if (!path.startsWith(MODULE_KEY)) {
                return;
            }
            Type type = new TypeToken<Map<String, JsonElement>>() {
            }.getType();
            String name = moduleJson.get("name").getAsString();
            Map<String, JsonElement> moduleProperties = new HashMap<>();
            Map<String, JsonElement> rawProperties = gson.fromJson(moduleJsonString, type);
            rawProperties.forEach((key, json) -> {
                if (isValidProperty(key, path, json, isClient)) {
                    moduleProperties.put(key, json);
                }
            });
            RegistryInventory.modules.register(name, new ItemModule(name, moduleProperties));
        } catch (Exception e) {
            LOGGER.warn("Could not load Module " + path, e);
        }
    }

    /**
     * Loads an ItemModule from a JSON string.
     *
     * @param path             the path of the JSON file
     * @param moduleJsonString the JSON string to load from
     */
    public static void loadModuleExtension(String path, String moduleJsonString, boolean isClient) {
        try {
            //TODO:rework this into SynergyManagers implementation
            JsonObject moduleJson = gson.fromJson(moduleJsonString, JsonObject.class);
            String name = moduleJson.get("name").getAsString();
            ItemModule module = RegistryInventory.modules.get(name);
            if (module == null) {
                LOGGER.warn("module not found to be extended! " + name);
                return;
            }
            Map<String, JsonElement> moduleProperties = new HashMap<>(module.properties());
            if (moduleJson.has("remove")) {
                moduleJson.get("remove").getAsJsonArray().forEach(jsonElement -> {
                    try {
                        moduleProperties.remove(jsonElement.getAsString());
                    } catch (Exception e) {
                    }
                });
            }
            if (moduleJson.has("merge")) {
                Map<String, JsonElement> rawMergeProperties = getPropertiesFromJsonString(moduleJson.get("merge"), path, isClient);
                rawMergeProperties.forEach((key, element) -> {
                    if (moduleProperties.containsKey(key)) {
                        ModuleProperty property = RegistryInventory.moduleProperties.get(key);
                        if (property != null) {
                            moduleProperties.put(key, property.merge(moduleProperties.get(key), element, MergeType.SMART));
                        }
                    } else {
                        moduleProperties.put(key, element);
                    }
                });
            }
            if (moduleJson.has("replace")) {
                Map<String, JsonElement> rawReplaceProperties = getPropertiesFromJsonString(moduleJson.get("replace"), path, isClient);
                moduleProperties.putAll(rawReplaceProperties);
            }
            RegistryInventory.modules.getFlatMap().remove(name);
            RegistryInventory.modules.register(name, new ItemModule(name, moduleProperties));
        } catch (Exception e) {
            LOGGER.warn("Could not load Module to extend " + path, e);
        }
    }

    protected static Map<String, JsonElement> getPropertiesFromJsonString(String jsonString, String debugPath, boolean isClient) {
        Map<String, JsonElement> moduleProperties = new HashMap<>();
        Type type = new TypeToken<Map<String, JsonElement>>() {
        }.getType();
        Map<String, JsonElement> rawProperties = gson.fromJson(jsonString, type);
        rawProperties.forEach((key, json) -> {
            if (isValidProperty(key, debugPath, json, isClient)) {
                moduleProperties.put(key, json);
            }
        });
        return moduleProperties;
    }

    protected static Map<String, JsonElement> getPropertiesFromJsonString(JsonElement jsonString, String debugPath, boolean isClient) {
        Map<String, JsonElement> moduleProperties = new HashMap<>();
        Type type = new TypeToken<Map<String, JsonElement>>() {
        }.getType();
        Map<String, JsonElement> rawProperties = gson.fromJson(jsonString, type);
        rawProperties.forEach((key, json) -> {
            if (isValidProperty(key, debugPath, json, isClient)) {
                moduleProperties.put(key, json);
            }
        });
        return moduleProperties;
    }

    /**
     * Processes a JSON element and adds valid properties to the module properties map.
     *
     * @param element          the JSON element to process
     * @param moduleProperties the map of properties for the module
     * @param name             the name of the module
     * @param path             the path of the JSON file
     * @param rawString        the raw JSON string
     */
    protected static void processModuleJsonElement(JsonElement element, Map<String, JsonElement> moduleProperties, String name, String path, String rawString, boolean isClient) {
        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String key = entry.getKey();
                JsonElement value = entry.getValue();
                ModuleProperty property = RegistryInventory.moduleProperties.get(key);
                if (property != null) {
                    if (isValidProperty(key, name, value, isClient)) {
                        moduleProperties.put(key, value);
                    }
                } else if (value.isJsonObject()) {
                    processModuleJsonElement(value, moduleProperties, name, path, rawString, isClient);
                } else if (value.isJsonArray()) {
                    JsonArray jsonArray = value.getAsJsonArray();
                    for (JsonElement jsonElement : jsonArray) {
                        processModuleJsonElement(jsonElement, moduleProperties, name, path, rawString, isClient);
                    }
                } else {
                    LOGGER.error("Error while reading ModuleJson, module " + name + " key/property " + key + " in file " + path + " Please make sure there are no Typos in the Property Names");
                }
            }
        }
    }

    /**
     * Checks if the given module property is valid and can be loaded.
     *
     * @param key       the key of the module property
     * @param moduleKey the key of the module
     * @param data      the data to load the module property
     * @return true if the module property is valid and can be loaded, false otherwise
     * @throws RuntimeException if an error occurs during loading
     */
    protected static boolean isValidProperty(String key, String moduleKey, JsonElement data, boolean isClient) {
        ModuleProperty property = RegistryInventory.moduleProperties.get(key);
        if (property != null) {
            try {
                return property.load(moduleKey, data);
                //if (!(property instanceof RenderProperty) || isClient) {
                //    return property.load(moduleKey, data, isClient);
                //} else {
                //    return true;
                //}
            } catch (Exception e) {
                RuntimeException exception = new RuntimeException("Failure during moduleLoad, Error in Module " + moduleKey + " with property " + key + " with data " + data + " with error " + e.getLocalizedMessage());
                exception.addSuppressed(e);
                throw exception;
            }
        } else {
            LOGGER.error("Module " + moduleKey + " contains invalid property " + key);
            LOGGER.error("This indicates either a broken Module, Outdated API version or missing dependency!");
        }
        return false;
    }

    /**
     * Gets the root module instance associated with the given ItemStack.
     *
     * @param stack the ItemStack to getRaw the module instance from
     * @return the module instance associated with the given ItemStack
     */
    public static ModuleInstance getModules(ItemStack stack) {
        if (ReloadEvents.isInReload()) {
            if (MiapiConfig.INSTANCE.server.other.verboseLogging) {
                //LOGGER.info("Item cannot have modules during a reload.");
            }
            return new ModuleInstance(ItemModule.empty);
        }
        if (stack.getItem() instanceof VisualModularItem && !ReloadEvents.isInReload()) {
            return stack.getComponents().get(ModuleInstance.componentType);
        }
        return new ModuleInstance(ItemModule.empty);
    }

    /**
     * Gets a map of unmerged module properties for the given module instance.
     *
     * @param modules the module instance to getRaw the unmerged module properties from
     * @return a map of unmerged module properties
     */
    public static Map<ItemModule, List<JsonElement>> getUnmergedProperties(ModuleInstance modules) {
        Map<ItemModule, List<JsonElement>> unmergedProperties = new HashMap<>();
        for (ModuleInstance module : modules.subModules.values()) {
            module.getProperties().forEach((property, data) -> {
                unmergedProperties.getOrDefault(property, new ArrayList<>()).add(data);
            });
        }
        return unmergedProperties;
    }

    /**
     * this method works through all submodules and merges the Properties using the Smart MergeType
     *
     * @param moduleInstance the root instance
     * @param property       the property to be merged
     * @return the merged PropertyJson
     */
    public static JsonElement getMergedProperty(ModuleInstance moduleInstance, ModuleProperty property) {
        return getMergedProperty(moduleInstance, property, MergeType.SMART);
    }

    public static JsonElement getMergedProperty(ItemStack itemStack, ModuleProperty property) {
        return getMergedProperty(getModules(itemStack), property, MergeType.SMART);
    }

    /**
     * this method works through all submodules and merges the Properties depending on the supplied MergeType
     *
     * @param moduleInstance the root instance
     * @param property       the property to be merged
     * @param type           the mergeType for the merge Logic
     * @return the merged PropertyJson
     */
    public static JsonElement getMergedProperty(ModuleInstance moduleInstance, ModuleProperty property, MergeType type) {
        JsonElement mergedProperty = null;
        for (ModuleInstance module : moduleInstance.allSubModules()) {
            JsonElement currentProperty = module.getProperties().get(property);
            if (currentProperty != null) {
                if (mergedProperty == null) {
                    mergedProperty = currentProperty;
                } else {
                    mergedProperty = property.merge(mergedProperty, currentProperty, type);
                }
            }
        }
        return mergedProperty;
    }

    /**
     * this method works through all submodules and merges the Properties depending on the supplied MergeType
     *
     * @param itemStack the ModularItemStack
     * @param property  the property to be merged
     * @param type      the mergeType for the merge Logic
     * @return the merged PropertyJson
     */
    public static JsonElement getMergedProperty(ItemStack itemStack, ModuleProperty property, MergeType type) {
        ModuleInstance moduleInstance = getModules(itemStack);
        JsonElement mergedProperty = null;
        for (ModuleInstance module : moduleInstance.allSubModules()) {
            JsonElement currentProperty = module.getProperties().get(property);
            if (currentProperty != null) {
                if (mergedProperty == null) {
                    mergedProperty = currentProperty;
                } else {
                    mergedProperty = property.merge(mergedProperty, currentProperty, type);
                }
            }
        }
        return mergedProperty;
    }

    /**
     * Creates a flat list of all modules starting from the specified root module.
     *
     * @param root the root module
     * @return the flat list of all modules
     */
    public static List<ModuleInstance> createFlatList(ModuleInstance root) {
        List<ModuleInstance> flatList = new ArrayList<>();
        List<ModuleInstance> queue = new ArrayList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            ModuleInstance module = queue.remove(0);
            if (module != null) {
                flatList.add(module);

                List<ModuleInstance> submodules = new ArrayList<>();
                module.subModules.keySet().stream().sorted((a, b) -> b - a).forEach(id -> {
                    submodules.add(module.subModules.get(id));
                });
                queue.addAll(0, submodules);
            }
        }

        return flatList;
    }


    /**
     * Returns whether this module is empty (i.e. equals the empty module).
     *
     * @return true if this module is empty, false otherwise
     */
    public boolean isEmpty() {
        return this.equals(empty);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ItemModule otherModule) {
            return this.name.equals(otherModule.name);
        }
        return false;
    }

}
