package smartin.miapi.modules;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.MiapiRegistry;
import smartin.miapi.registries.RegistryInventory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static smartin.miapi.Miapi.LOGGER;
import static smartin.miapi.Miapi.gson;

/**
 * An ItemModule represents a Module loaded from a JSON
 */
public class ItemModule {
    public static final MiapiRegistry<ItemModule> moduleRegistry = MiapiRegistry.getInstance(ItemModule.class);
    /**
     * The name of the module.
     */
    private final String name;
    /**
     * The map of properties for the module.
     */
    private final Map<String, JsonElement> properties;
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
     * Creates an instance of ItemModule with a given name and map of properties.
     *
     * @param name       the name of the module
     * @param properties the map of properties for the module
     */
    public ItemModule(String name, Map<String, JsonElement> properties) {
        this.name = name;
        this.properties = properties;
    }

    /**
     * Returns the map of properties for this module.
     *
     * @return the map of properties for this module
     */
    public Map<String, JsonElement> getProperties() {
        return properties;
    }

    public Map<ModuleProperty, JsonElement> getKeyedProperties() {
        HashMap<ModuleProperty, JsonElement> map = new HashMap<>();
        getProperties().forEach((key, jsonData) -> map.put(RegistryInventory.moduleProperties.get(key), jsonData));
        return map;
    }

    /**
     * Returns the name of this module.
     *
     * @return the name of this module
     */
    public String getName() {
        return name;
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

            moduleRegistry.register(name, new ItemModule(name, moduleProperties));
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
            ItemModule module = moduleRegistry.get(name);
            if (module == null) {
                LOGGER.warn("module not found to be extended! " + name);
                return;
            }
            Map<String, JsonElement> moduleProperties = new HashMap<>(module.getProperties());
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
            moduleRegistry.getFlatMap().remove(name);
            moduleRegistry.register(name, new ItemModule(name, moduleProperties));
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
            return new ModuleInstance(new ItemModule("empty", new HashMap<>()));
        }
        if (stack.getItem() instanceof VisualModularItem && !ReloadEvents.isInReload() && (stack.getOrCreateNbt().get(MODULE_KEY) != null || stack.getOrCreateNbt().get(ItemModule.NBT_MODULE_KEY) != null)) {
            ModuleInstance moduleInstance = ModularItemCache.getRaw(stack, MODULE_KEY);
            if (moduleInstance == null || moduleInstance.module == null) {
                IllegalArgumentException exception = new IllegalArgumentException("Item has Invalid Module onReload - treating it like it has no modules");
                LOGGER.warn("Item has Invalid Module onReload - treating it like it has no modules", exception);
                return new ModuleInstance(new ItemModule("empty", new HashMap<>()));
            }
            return moduleInstance;
        }
        return new ModuleInstance(new ItemModule("empty", new HashMap<>()));
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

    public static class ModuleInstanceJsonAdapter extends TypeAdapter<ModuleInstance> {
        @Override
        public void write(JsonWriter out, ModuleInstance value) throws IOException {
            out.beginObject();
            if (value.module.name != null) {
                out.name("module").value(value.module.name);
                if (value.moduleData != null) {
                    out.name("moduleData").jsonValue(gson.toJson(value.moduleData));
                } else {
                    Map<String, String> moduleData = new HashMap<>();
                    out.name("moduleData").jsonValue(gson.toJson(moduleData));
                }
                if (value.subModules != null) {
                    out.name("subModules").jsonValue(gson.toJson(value.subModules));
                } else {
                    Map<String, String> subModules = new HashMap<>();
                    out.name("subModules").jsonValue(gson.toJson(subModules));
                }
            }
            out.endObject();
        }

        @Override
        public ModuleInstance read(JsonReader in) throws IOException {
            JsonObject jsonObject = JsonParser.parseReader(in).getAsJsonObject();
            String moduleKey = jsonObject.get("module").getAsString();
            ItemModule module = moduleRegistry.get(moduleKey);
            if (module == null) {
                LOGGER.warn("Module not found for " + moduleKey + " Key - substituting with empty module");
                module = ItemModule.empty;
            }
            ModuleInstance moduleInstance = new ModuleInstance(module);
            moduleInstance.subModules = gson.fromJson(jsonObject.get("subModules"), new TypeToken<Map<Integer, ModuleInstance>>() {
            }.getType());
            if (moduleInstance.subModules != null) {
                moduleInstance.subModules.forEach((key, subModule) -> {
                    subModule.parent = moduleInstance;
                });
            } else {
                moduleInstance.subModules = new HashMap<>();
            }
            moduleInstance.moduleData = gson.fromJson(jsonObject.get("moduleData"), new TypeToken<Map<String, String>>() {
            }.getType());
            if (moduleInstance.moduleData == null) {
                moduleInstance.moduleData = new HashMap<>();
            }
            return moduleInstance;
        }
    }

    /**
     * A class representing a single module instance that belongs to an item.
     */
    @JsonAdapter(ModuleInstanceJsonAdapter.class)
    public static class ModuleInstance {
        /**
         * The item module represented by this module instance.
         */
        public ItemModule module;
        /**
         * The parent module instance of this module instance, if any.
         */
        @Nullable
        public ItemModule.ModuleInstance parent;
        /**
         * A map of child module instances to their respective module IDs.
         */
        public Map<Integer, ModuleInstance> subModules = new HashMap<>();
        /**
         * A map of module data keys to their respective values.
         */
        public Map<String, String> moduleData = new HashMap<>();

        /**
         * A map of the raw properties. Only access this when you know what you are doing.
         */
        @Nullable
        public Map<ModuleProperty, JsonElement> rawProperties;

        /**
         * Constructs a new module instance with the given item module.
         *
         * @param module the item module for the module instance
         */
        public ModuleInstance(ItemModule module) {
            if (module == null) {
                this.module = ItemModule.empty;
            } else {
                this.module = module;
            }
        }

        /**
         * Returns a flat list of all sub-modules, including the current module instance.
         *
         * @return a list of all sub-modules
         */
        public List<ModuleInstance> allSubModules() {
            return createFlatList(this);
        }

        /**
         * Returns a map of all properties and their associated JSON elements for this module instance and its sub-modules.
         *
         * @return a map of module properties and their associated JSON elements
         */
        public Map<ModuleProperty, JsonElement> getProperties() {
            if (rawProperties == null) {
                PropertyResolver.resolve(this.getRoot());
            }
            return rawProperties;
        }

        /**
         * Returns a map of all properties and their associated JSON elements for this module instance,
         * keyed by the property names.
         *
         * @return a map of module properties and their associated JSON elements, keyed by property name
         */
        public Map<String, JsonElement> getKeyedProperties() {
            Map<String, JsonElement> map = new HashMap<>();
            getProperties().forEach((property, jsonElement) -> {
                map.put(RegistryInventory.moduleProperties.findKey(property), jsonElement);
            });
            return map;
        }

        /**
         * Returns a map of all properties and their associated JSON elements for this module instance and all its submodules
         * keyed by the property names.
         *
         * @return a map of module properties and their associated JSON elements, keyed by property name
         */
        public Map<ModuleProperty, JsonElement> getPropertiesMerged() {
            Map<ModuleProperty, JsonElement> map = new ConcurrentHashMap<>();
            for (ModuleInstance moduleInstance : this.allSubModules()) {
                moduleInstance.getProperties().forEach((property, element) -> {
                    if (map.containsKey(property)) {
                        try {
                            map.put(property, property.merge(map.get(property), element, MergeType.SMART));
                        } catch (Exception e) {
                            LOGGER.error("coudlnt merge " + property, e);
                            map.put(property, element);
                        }
                    } else {
                        map.put(property, element);
                    }
                });
            }
            return map;
        }

        /**
         * Returns the root module instance, i.e., the module instance that has no parent.
         *
         * @return the root module instance
         */
        public ModuleInstance getRoot() {
            ModuleInstance root = this;
            while (root.parent != null) {
                root = root.parent;
            }
            return root;
        }

        /**
         * Creates a copy of this module instance and all its parents and children.
         *
         * @return The copied module instance.
         */
        public ModuleInstance copy() {
            List<Integer> position = new ArrayList<>();
            calculatePosition(position);

            ModuleInstance root = this.getRoot().deepCopy();

            return root.getPosition(position);
        }

        /**
         * Recursively calculates the position of this module instance in its hierarchy.
         *
         * @param position The list to store the position.
         */
        public void calculatePosition(List<Integer> position) {
            if (parent != null) {
                parent.calculatePosition(position);
                position.add(this.getId());
            }
        }

        /**
         * Retrieves the module instance at the specified position in the hierarchy.
         *
         * @param position The position of the module instance.
         * @return The module instance at the specified position.
         */
        public ModuleInstance getPosition(List<Integer> position) {
            if (!position.isEmpty()) {
                int pos = position.remove(0);
                ModuleInstance subModule = subModules.get(pos);
                if (subModule != null) {
                    return subModule.getPosition(position);
                }
            }
            return this;
        }

        /**
         * Retrieves the ID of this module instance.
         *
         * @return The ID of the module instance, or null if not found.
         */
        @Nullable
        public Integer getId() {
            if (parent != null) {
                for (Map.Entry<Integer, ModuleInstance> entry : parent.subModules.entrySet()) {
                    if (entry.getValue() == this) {
                        return entry.getKey();
                    }
                }
            }
            return null;
        }

        /**
         * Creates a deep copy of this module instance and its submodules.
         *
         * @return The copied module instance.
         */
        private ModuleInstance deepCopy() {
            ModuleInstance copy = new ModuleInstance(this.module);
            copy.moduleData = new HashMap<>(this.moduleData);
            this.subModules.forEach(((id, subModule) -> {
                ModuleInstance subModuleCopy = subModule.deepCopy();
                subModuleCopy.parent = copy;
                copy.subModules.put(id, subModuleCopy);
            }));
            return copy;
        }

        /**
         * Writes the module to the item using the current module.
         *
         * @param stack The ItemStack to write the module to.
         */
        public void writeToItem(ItemStack stack) {
            writeToItem(stack, true);
        }

        /**
         * Writes the module to the item using the current module.
         *
         * @param stack      The ItemStack to write the module to.
         * @param clearCache Determines whether to clear the cache after writing the module.
         */
        public void writeToItem(ItemStack stack, boolean clearCache) {
            if (clearCache) {
                ModularItemCache.clearUUIDFor(stack);
            }
            stack.getOrCreateNbt().putString(ItemModule.NBT_MODULE_KEY, this.toString());
            if (stack.getOrCreateNbt().contains(MODULE_KEY)) {
                stack.getOrCreateNbt().remove(MODULE_KEY);
            }
            //stack.getOrCreateNbt().putString(ItemModule.MODULE_KEY, this.toString());
            if (clearCache) {
                ModularItemCache.clearUUIDFor(stack);
            }
        }

        /**
         * Returns a JSON string representation of this module instance.
         *
         * @return a JSON string representation of this module instance
         */
        public String toString() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }

        /**
         * Returns a module instance constructed from the given JSON string representation.
         *
         * @param string the JSON string representation of a module instance
         * @return a module instance constructed from the given JSON string representation
         */
        public static ModuleInstance fromString(String string) {
            Gson gson = new Gson();
            ModuleInstance moduleInstance = gson.fromJson(string, ModuleInstance.class);
            if (moduleInstance.module == null) {
                moduleInstance.module = empty;
            }
            return moduleInstance;
        }

        @Nullable
        public ModuleInstance parseTo(String[] data) {
            if (data.length == 0) {
                return this;
            }
            String[] newArray = Arrays.copyOfRange(data, 1, data.length);
            if ("parent".equals(data[0])) {
                if (this.parent != null) {
                    return parent.parseTo(newArray);
                }
            } else {
                try {
                    int id = Integer.parseInt(data[0]);
                    if (subModules.containsKey(id)) {
                        subModules.get(0).parseTo(newArray);
                    }
                } catch (NumberFormatException ignored) {

                }
            }
            return null;
        }
    }
}
