package smartin.miapi.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.modules.synergies.SynergyManager;
import smartin.miapi.registries.RegistryInventory;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;

import static smartin.miapi.Miapi.LOGGER;
import static smartin.miapi.Miapi.gson;

/**
 * An ItemModule represents a Module loaded from a JSON
 *
 * @param name       The name of the module.
 * @param properties The map of properties for the module.
 */
public record ItemModule(ResourceLocation name, Map<ModuleProperty<?>, Object> properties) {

    /**
     * The key for the properties in the Cache.
     */
    public static final String MODULE_KEY = "modules";
    /**
     * An empty ItemModule instance.
     */
    public static final ItemModule empty = new ItemModule(Miapi.id("empty"), new HashMap<>());

    /**
     * An internal ItemModule instance, can be used for whatever purpose
     */
    public static final ItemModule internal = new ItemModule(Miapi.id("internal"), new HashMap<>());


    /**
     * Loads an ItemModule from a JSON string.
     *
     * @param path             the path of the JSON file
     * @param moduleJsonString the JSON string to load from
     */
    public static void loadFromData(ResourceLocation path, String moduleJsonString, boolean isClient) {
        try {
            JsonObject moduleJson = gson.fromJson(moduleJsonString, JsonObject.class);
            Type type = new TypeToken<Map<String, JsonElement>>() {
            }.getType();
            Map<ModuleProperty<?>, Object> decodedProperties = new HashMap<>();
            String id = path.toString();
            id = id.replace("miapi/modules/", "");
            id = id.replace(".json", "");
            ResourceLocation revisedID = ResourceLocation.parse(id);
            LOGGER.info("registered Module " + revisedID);
            Map<String, JsonElement> rawProperties = gson.fromJson(moduleJsonString, type);
            rawProperties.forEach((key, json) -> isValidProperty(key, path, json, isClient, (pair) -> decodedProperties.put(pair.getFirst(), pair.getSecond())));
            RegistryInventory.modules.register(revisedID.toString(), new ItemModule(revisedID, decodedProperties));
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
    public static void loadModuleExtension(ResourceLocation path, String moduleJsonString, boolean isClient) {
        try {
            JsonObject moduleJson = gson.fromJson(moduleJsonString, JsonObject.class);
            SynergyManager.PropertyHolder holder = SynergyManager.getFrom(moduleJson, isClient, path);
            String name = moduleJson.get("name").getAsString();
            ItemModule module = RegistryInventory.modules.get(name);
            if (module == null) {
                LOGGER.warn("module not found to be extended! " + name);
                return;
            }
            RegistryInventory.modules.getFlatMap().remove(name);
            Map<ModuleProperty<?>, Object> map = holder.applyHolder(module.properties());
            if (map == null) {
                map = new HashMap<>();
                LOGGER.warn("is NULL wtf holder.applyHolder is false");
            }
            RegistryInventory.modules.register(name, new ItemModule(Miapi.id(name), map));
        } catch (Exception e) {
            LOGGER.warn("Could not load Module to extend " + path, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T merge(ModuleProperty<T> property, Object left, Object right, MergeType mergeType) {
        return property.merge((T) left, (T) right, mergeType);
    }

    /**
     * Checks if the given module property is valid and can be loaded.
     *
     * @param key  the key of the module property
     * @param path the path of the module
     * @param data the data to load the module property
     */
    @SuppressWarnings("unchecked")
    private static void isValidProperty(String key, ResourceLocation path, JsonElement data, boolean isClient, Consumer<Pair<ModuleProperty<?>, Object>> onValid) {
        ModuleProperty<?> property = RegistryInventory.moduleProperties.get(key);
        if (property != null) {
            try {
                boolean valid = property.load(Miapi.id(key), data, isClient);
                if (valid) {
                    onValid.accept(new Pair<>(property, property.decode(data)));
                }
            } catch (Exception e) {
                LOGGER.error("Failure during moduleLoad, Error in Module " + path.toString() + " with property " + key + " with data " + data + "with error " + e.getLocalizedMessage(), e);
            }
        } else {
            LOGGER.error("Module " + path + " contains invalid property " + key);
            LOGGER.error("This indicates either a broken Module, Outdated API version or missing dependency!");
        }
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
                LOGGER.info("Item cannot have modules during a reload.");
            }
            return new ModuleInstance(ItemModule.empty);
        }
        if (stack.getItem() instanceof VisualModularItem && !ReloadEvents.isInReload()) {
            return stack.getComponents().get(ModuleInstance.MODULE_INSTANCE_COMPONENT);
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
            ModuleInstance module = queue.removeFirst();
            if (module != null) {
                flatList.add(module);

                List<ModuleInstance> submodules = new ArrayList<>();
                //TODO:add prioritized sorting into slot logic
                module.subModules.keySet().stream().sorted().forEach(id -> {
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
