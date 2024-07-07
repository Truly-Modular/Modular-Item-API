package smartin.miapi.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static smartin.miapi.Miapi.LOGGER;
import static smartin.miapi.Miapi.gson;

/**
 * An ItemModule represents a Module loaded from a JSON
 *
 * @param name       The name of the module.
 * @param properties The map of properties for the module.
 */
public record ItemModule(String name, Map<ModuleProperty<?>, Object> properties) {

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

    /**
     * An internal ItemModule instance, can be used for whatever purpose
     */
    public static final ItemModule internal = new ItemModule("internal", new HashMap<>());


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
            Map<ModuleProperty<?>, Object> decodedProperties = new HashMap<>();
            Map<String, JsonElement> rawProperties = gson.fromJson(moduleJsonString, type);
            rawProperties.forEach((key, json) -> {
                if (isValidProperty(key, path, json, isClient, (pair) -> decodedProperties.put(pair.getFirst(), pair.getSecond()))) {
                    moduleProperties.put(key, json);
                }
            });
            RegistryInventory.modules.register(name, new ItemModule(name, decodedProperties));
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
            Map<ModuleProperty<?>, Object> moduleProperties = new HashMap<>(module.properties());
            if (moduleJson.has("remove")) {
                moduleJson.get("remove").getAsJsonArray().forEach(jsonElement -> {
                    try {
                        ModuleProperty<?> property = RegistryInventory.moduleProperties.get(jsonElement.getAsString());
                        moduleProperties.remove(property);
                    } catch (Exception e) {
                    }
                });
            }
            if (moduleJson.has("merge")) {
                Map<ModuleProperty<?>, Object> rawMergeProperties = getPropertiesFromJsonString(moduleJson.get("merge"), path, isClient);
                rawMergeProperties.forEach((key, element) -> {
                    if (moduleProperties.containsKey(key)) {
                        moduleProperties.put(key, merge(key, moduleProperties.get(key), module, MergeType.SMART));
                    } else {
                        moduleProperties.put(key, element);
                    }
                });
            }
            if (moduleJson.has("replace")) {
                Map<ModuleProperty<?>, Object> rawReplaceProperties = getPropertiesFromJsonString(moduleJson.get("replace"), path, isClient);
                moduleProperties.putAll(rawReplaceProperties);
            }
            RegistryInventory.modules.getFlatMap().remove(name);
            RegistryInventory.modules.register(name, new ItemModule(name, moduleProperties));
        } catch (Exception e) {
            LOGGER.warn("Could not load Module to extend " + path, e);
        }
    }

    public static <T> T merge(ModuleProperty<T> property, Object left, Object right, MergeType mergeType) {
        return property.merge((T) left, (T) right, mergeType);
    }

    protected static Map<ModuleProperty<?>, Object> getPropertiesFromJsonString(JsonElement jsonString, String debugPath, boolean isClient) {
        Map<ModuleProperty<?>, Object> moduleProperties = new HashMap<>();
        Type type = new TypeToken<Map<String, JsonElement>>() {
        }.getType();
        Map<String, JsonElement> rawProperties = gson.fromJson(jsonString, type);
        rawProperties.forEach((key, json) -> {
            if (isValidProperty(key, debugPath, json, isClient, (pair) -> moduleProperties.put(pair.getFirst(), pair.getSecond()))) {
            }
        });
        return moduleProperties;
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
    protected static boolean isValidProperty(String key, String moduleKey, JsonElement data, boolean isClient, Consumer<Pair<ModuleProperty<?>, Object>> onValid) {
        ModuleProperty property = RegistryInventory.moduleProperties.get(key);
        if (property != null) {
            try {
                return property.load(Miapi.id(moduleKey), data, isClient);
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
