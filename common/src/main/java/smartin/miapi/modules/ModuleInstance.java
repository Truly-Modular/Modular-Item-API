package smartin.miapi.modules;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static smartin.miapi.Miapi.LOGGER;

/**
 * A class representing a single module instance that belongs to an item.
 */
@JsonAdapter(ItemModule.ModuleInstanceJsonAdapter.class)
public class ModuleInstance {
    /**
     * The item module represented by this module instance.
     */
    public ItemModule module;
    /**
     * The parent module instance of this module instance, if any.
     */
    @Nullable
    public ModuleInstance parent;
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
        this.module = module;
    }

    /**
     * Returns a flat list of all sub-modules, including the current module instance.
     *
     * @return a list of all sub-modules
     */
    public List<ModuleInstance> allSubModules() {
        return ItemModule.createFlatList(this);
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
        Map<ModuleProperty, JsonElement> map = new HashMap<>();
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
        stack.getOrCreateNbt().putString("modules", this.toString());
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
            moduleInstance.module = ItemModule.empty;
        }
        return moduleInstance;
    }
}
