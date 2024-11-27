package smartin.miapi.modules;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redpxnda.nucleus.util.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.material.Material;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.mixin.RegistryOpsAccessor;
import smartin.miapi.modules.cache.DataCache;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.slot.SlotProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A class representing a single module instance that belongs to an item.
 * The rest of this Javadoc is used to populate the Wiki
 *
 * @header ModuleInstance Component
 * @path /components/module
 * @description_start This Component stores the main Modules for the Itemstack in question.
 * This Component can be attached to any Itemstack, but many of the properties only correctly work on Modular Items,
 * so It's recommended to only attach his to Modular Item
 * @description_end
 * @data key:the ID of the module to be attached
 * @data child: a slotId -> submodule map to map all submodules. This will be sorted in relation to the Slot Properties priority.
 * @data data: this is a String->custom data map to store additional custom data for properties to use. This is a per module savedata system.
 */
public class ModuleInstance {
    public static Codec<ModuleInstance> CODEC;
    public static DataComponentType<ModuleInstance> MODULE_INSTANCE_COMPONENT;

    static {
        Codec<Map<ResourceLocation, JsonElement>> dataJsonCodec = Codec.unboundedMap(Miapi.ID_CODEC, StatResolver.Codecs.JSONELEMENT_CODEC).xmap((i) -> i, Function.identity());
        Codec<ModuleInstance> basicCodec = Codec.recursive(
                "module_instance",
                selfCodec -> RecordCodecBuilder.create((instance) ->
                        instance.group(
                                ResourceLocation.CODEC.fieldOf("key")
                                        .forGetter((moduleInstance) -> moduleInstance.moduleID),
                                Codec.unboundedMap(Codec.STRING, selfCodec).xmap((i) -> i, Function.identity())
                                        .optionalFieldOf("child", new LinkedHashMap<>())
                                        .forGetter((moduleInstance) -> moduleInstance.subModules),
                                dataJsonCodec
                                        .optionalFieldOf("data", new HashMap<>())
                                        .forGetter((moduleInstance) -> moduleInstance.moduleData)
                        ).apply(instance, ModuleInstance::new))
        );
        CODEC = new Codec<ModuleInstance>() {
            @Override
            public <T> DataResult<Pair<ModuleInstance, T>> decode(DynamicOps<T> ops, T input) {
                var basicResult = basicCodec.decode(ops, input);
                if (ops instanceof RegistryOps<T> registryOps) {
                    if (basicResult.isSuccess()) {
                        basicResult.getOrThrow().getFirst().lookup = ((RegistryOpsAccessor) registryOps).getLookupProvider();
                    }
                    //TODO:give moduleinstances guaranteeed registry access
                }
                var result = basicResult.getOrThrow().getFirst();
                if (result.subModules.isEmpty() && result.module != ItemModule.empty) {
                    //Miapi.LOGGER.error("possible problem!");
                    //Miapi.LOGGER.warn("encoded module " + result);
                }
                return basicResult;
            }

            Color color;

            @Override
            public <T> DataResult<T> encode(ModuleInstance input, DynamicOps<T> ops, T prefix) {
                var result = basicCodec.encode(input, ops, prefix);
                if (input.subModules.isEmpty() && input.module != ItemModule.empty) {
                    //Miapi.LOGGER.error("possible problem!");
                    //Miapi.LOGGER.warn("encoded module " + result.getOrThrow());
                }
                return result;
            }
        };
        MODULE_INSTANCE_COMPONENT = DataComponentType.<ModuleInstance>builder().persistent(CODEC).networkSynchronized(ByteBufCodecs.fromCodec(CODEC)).build();
    }

    @Nullable
    public RegistryAccess registryAccess;

    @Nullable
    public RegistryOps.RegistryInfoLookup lookup;

    /**
     * this is kept separately in case the module doesn't exist, otherwise the id would be defaulted to empty in case no module is found, permanently breaking the item
     */
    public ResourceLocation moduleID;

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
    public Map<String, ModuleInstance> subModules = new LinkedHashMap<>();
    /**
     * A map of module data keys to their respective values.
     */
    public Map<ResourceLocation, JsonElement> moduleData = new HashMap<>();


    /**
     * A map of the raw properties.
     * Only access this when you know what you are doing.
     * Use {@link ModuleInstance#getProperty(ModuleProperty)} instead to trigger the Property resolver
     */
    public Map<ModuleProperty<?>, Object> properties = null;

    /**
     * A map of the raw properties.
     * Only access this when you know what you are doing.
     * Use {@link ModuleInstance#getProperty(ModuleProperty)} instead to trigger the Property resolver
     */
    public Map<ModuleProperty<?>, Object> initializedProperties = new ConcurrentHashMap<>();

    /**
     * A map of the raw properties.
     * Only access this when you know what you are doing.
     * Use {@link ModuleInstance#getProperty(ModuleProperty)} instead to trigger the Property resolver
     */
    public Map<ModuleProperty<?>, Object> itemMergedProperties = new ConcurrentHashMap<>();


    public Map<String, Object> cachedData = new ConcurrentHashMap<>();
    public Map<String, Object> itemStackCache = new ConcurrentHashMap<>();

    /**
     * Constructs a new module instance with the given item module.
     *
     * @param module the item module for the module instance
     */
    public ModuleInstance(ItemModule module) {
        this.moduleID = module.id();
        this.module = module;
    }

    /**
     * Constructs a new module instance with the given item module.
     *
     * @param module the item module for the module instance
     */
    public ModuleInstance(ResourceLocation module, Map<String, ModuleInstance> subModules, Map<ResourceLocation, JsonElement> data) {
        this.moduleID = module;
        this.subModules = subModules;
        this.moduleData = new HashMap<>(data);
        subModules.values().forEach(subModule -> subModule.parent = this);
        this.module = RegistryInventory.modules.get(module.toString());
        if (this.module == null) {
            this.module = ItemModule.empty;
            Miapi.LOGGER.warn("could not find module " + module + " substituting with empty module");
        }
        sortSubModule();
    }

    /**
     * Returns a flat list of all sub-modules, including the current module instance.
     *
     * @return a list of all sub-modules
     */
    public List<ModuleInstance> allSubModules() {
        return ItemModule.createFlatList(this);
    }

    public void setSubModule(String id, ModuleInstance submodule) {
        removeSubModule(id);
        subModules.put(id, submodule);
        submodule.parent = this;
        sortSubModule();
        getRoot().clearCaches();
    }

    public void removeSubModule(String id) {
        ModuleInstance oldModule = subModules.get(id);
        if (oldModule != null) {
            //oldModule.parent = null;
        }
        subModules.remove(id);
        getRoot().clearCaches();
    }

    public Map<String, ModuleInstance> getSubModuleMap() {
        return new LinkedHashMap<>(subModules);
    }

    @Nullable
    public ModuleInstance getSubModule(String id) {
        return subModules.get(id);
    }

    @Nullable
    public ModuleInstance getParent() {
        return parent;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getProperty(ModuleProperty<T> property) {
        Object propertyData = initializedProperties.get(property);
        if (propertyData != null) {
            return (T) propertyData;
        }

        if (properties == null) {
            PropertyResolver.resolve(this.getRoot());
        }

        Object propertyDataRaw = properties.get(property);
        if (propertyDataRaw != null) {
            T data = (T) propertyDataRaw;
            data = property.initialize(data, this);
            this.initializedProperties.put(property, data);
            return data;
        }
        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getPropertyItemStack(ModuleProperty<T> property) {
        if (itemMergedProperties.containsKey(property)) {
            return (T) itemMergedProperties.get(property);
        }
        T propertyData = null;
        ModuleInstance lastDataOwner = null;
        ModuleInstance root = getRoot();
        for (ModuleInstance moduleInstance : root.allSubModules()) {
            T toMergeData = moduleInstance.getProperty(property);
            if (toMergeData != null) {
                if (propertyData == null) {
                    propertyData = toMergeData;
                    lastDataOwner = moduleInstance;
                } else {
                    propertyData = property.merge(propertyData, lastDataOwner, toMergeData, moduleInstance, MergeType.SMART);
                }
            }
        }
        if (property != null && propertyData != null) {
            itemMergedProperties.put(property, propertyData);
        }
        return propertyData;
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
        List<String> position = new ArrayList<>();
        calculatePosition(position);

        ModuleInstance root = this.getRoot().deepCopy();
        root.registryAccess = this.registryAccess;

        return root.getPosition(position);
    }

    /**
     * Recursively calculates the position of this module instance in its hierarchy.
     *
     * @param position The list to store the position.
     */
    public void calculatePosition(List<String> position) {
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
    public ModuleInstance getPosition(List<String> position) {
        if (!position.isEmpty()) {
            String pos = position.removeFirst();
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
    public String getId() {
        if (parent != null) {
            for (Map.Entry<String, ModuleInstance> entry : parent.subModules.entrySet()) {
                if (entry.getValue() == this) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public void sortSubModule() {
        SlotProperty.getInstance().getData(this.module);
        Map<String, ModuleInstance> sortedMap = new LinkedHashMap<>();
        SlotProperty.asSortedList(SlotProperty.getInstance().getData(this.module).orElse(new LinkedHashMap<>())).forEach(slot -> {
            ModuleInstance moduleInstance = subModules.get(slot.id);
            if (moduleInstance != null) {
                moduleInstance.parent = this;
                sortedMap.put(slot.id, moduleInstance);
            }
        });
        subModules = sortedMap;
    }

    /**
     * Creates a deep copy of this module instance and its submodules.
     *
     * @return The copied module instance.
     */
    private ModuleInstance deepCopy() {
        ModuleInstance copy = new ModuleInstance(this.module);
        copy.registryAccess = this.registryAccess;
        copy.moduleData = new HashMap<>(this.moduleData);
        this.subModules.forEach(((id, subModule) -> {
            ModuleInstance subModuleCopy = subModule.deepCopy();
            copy.setSubModule(id, subModuleCopy);
            subModule.registryAccess = this.registryAccess;
        }));
        return copy;
    }

    /**
     * Writes the module to the item using the current module.
     *
     * @param stack The ItemStack to encode the module to.
     */
    public void writeToItem(ItemStack stack) {
        this.clearCaches();
        writeToItem(stack, true);
    }

    /**
     * Writes the module to the item using the current module.
     *
     * @param stack      The ItemStack to encode the module to.
     * @param clearCache Determines whether to clear the cache after writing the module.
     */
    public void writeToItem(ItemStack stack, boolean clearCache) {
        if (clearCache) {
            this.clearCaches();
        }
        //stack.update(ModuleInstance.MODULE_INSTANCE_COMPONENT, this, (component) -> this);
        stack.set(ModuleInstance.MODULE_INSTANCE_COMPONENT, this.copy());
    }

    /**
     * Clears all cached data of this ModuleInstance and all its related ModulesInstances
     * should be used in case of changes to the item
     */
    public void clearCaches() {
        this.getRoot().allSubModules().forEach(ModuleInstance::clearCachesOnlyThis);
    }

    /**
     * Clears all cached data of this ModuleInstance
     * this should not be called directly.
     * Calling this directly will cause issues,
     * as the {@link PropertyResolver} will partially trigger if not all {@link ModuleInstance}
     * are cleared. {@link ModuleInstance#clearCaches()} should be used
     */
    public void clearCachesOnlyThis() {
        properties = null;
        itemMergedProperties.clear();
        cachedData.clear();
        itemStackCache.clear();
        initializedProperties.clear();
    }

    /**
     * Clears all cached data of this ModuleInstance
     * unlike like the other Cache-clears this doesn`t reset the Property Resolver, but still kills the initalized Properties
     */
    public void clearCachesSoftOnlyThis() {
        cachedData.clear();
        itemStackCache.clear();
        initializedProperties.clear();
    }

    @Environment(EnvType.CLIENT)
    public Component getModuleName() {
        String moduleName = module.id().toString();
        moduleName = moduleName.replace(":", ".");
        moduleName = moduleName.replaceAll("/", ".");
        Material material = MaterialProperty.getMaterial(this);
        if (material != null) {
            return Component.translatable(Miapi.MOD_ID + ".module." + moduleName, material.getTranslation());
        }
        return StatResolver.translateAndResolve(Miapi.MOD_ID + ".module." + moduleName, this);
    }

    @Environment(EnvType.CLIENT)
    public Component getModuleDescription() {
        String moduleName = module.id().toString();
        moduleName = moduleName.replace(":", ".");
        moduleName = moduleName.replaceAll("/", ".");
        Material material = MaterialProperty.getMaterial(this);
        if (material != null) {
            return Component.translatable(Miapi.MOD_ID + ".module." + moduleName + ".description", material.getTranslation());
        }
        return StatResolver.translateAndResolve(Miapi.MOD_ID + ".module." + moduleName + ".description", this);
    }

    public Optional<ModuleInstance> parseTo(String[] data) {
        if (data.length == 0) {
            return Optional.of(this);
        }
        String[] newArray = Arrays.copyOfRange(data, 1, data.length);
        if ("parent".equals(data[0])) {
            if (this.parent != null) {
                return parent.parseTo(newArray);
            }
        } else {
            try {
                String id = data[0];
                if (subModules.containsKey(id)) {
                    return subModules.get(id).parseTo(newArray);
                }
            } catch (NumberFormatException ignored) {

            }
        }
        return Optional.empty();
    }

    public String[] getStringPosition() {
        List<String> position = new ArrayList<>();
        ModuleInstance parser = this;
        while (this.parent != null) {
            ModuleInstance filter = parser;
            Optional<Map.Entry<String, ModuleInstance>> next = this
                    .parent
                    .subModules
                    .entrySet()
                    .stream()
                    .filter(e -> e.getValue().equals(filter))
                    .findFirst();
            if (next.isEmpty()) {
                return position.toArray(new String[]{});
            }
            position.add(next.get().getKey());
            parser = next.get().getValue();
        }
        return position.toArray(new String[]{});
    }

    /**
     * this function is meant to be used {@link ModularItemCache#MODULE_CACHE_SUPPLIER}
     * to have on demand caching on a per {@link ModuleInstance} level
     * if Itemstack Level caching is desired, {@link ModularItemCache#get(ItemStack, String, Object)} should be looked at
     *
     * @param key      the Key for the stored data. Common Practice is to use {@link net.minecraft.resources.ResourceLocation} stringifies for this
     * @param fallback a supplier of a fallback in case this cant be resolved. Stuff cannot be resolved during reloads or other invalid stats.
     * @param <T>      the Type of the data in question, used to avoid casting
     * @return Returns the Cached data if available, otherwise uses the registered {@link ModularItemCache#MODULE_CACHE_SUPPLIER} to supply and then cache the data
     */
    @SuppressWarnings("unchecked")
    public <T> T getFromCache(String key, Supplier<T> fallback) {
        T data = (T) cachedData.get(key);
        if (data != null) {
            return data;
        }
        DataCache.ModuleCacheSupplier cacheSupplier = ModularItemCache.MODULE_CACHE_SUPPLIER.get(key);
        if (cacheSupplier != null) {
            data = (T) cacheSupplier.apply(this);
            if (data != null) {
                cachedData.put(key, data);
                return data;
            }
        }
        return fallback.get();
    }

    /**
     * returns the Item-level Cache for this. Itemstack is required as context
     *
     * @param key       the key under {@link ModularItemCache#setSupplier(String, ModularItemCache.CacheObjectSupplier)} the supplier was registered
     * @param itemStack the Context Itemstack
     * @param fallback  fallback value in case the state was invalid or the supplier returned null
     * @param <T>       the type inside the cache
     * @return the cached value
     */
    public <T> T getFromCache(String key, ItemStack itemStack, T fallback) {
        return ModularItemCache.get(itemStack, key, fallback);
    }

    /**
     * returns the Item-level Cache for this. Itemstack is required as context
     *
     * @param key       the key under {@link ModularItemCache#setSupplier(String, ModularItemCache.CacheObjectSupplier)} the supplier was registered
     * @param itemStack the Context Itemstack
     * @param fallback  fallback value in case the state was invalid or the supplier returned null
     * @param <T>       the type inside the cache
     * @return the cached value
     */
    public <T> T getFromCache(String key, ItemStack itemStack, Supplier<T> fallback) {
        return ModularItemCache.get(itemStack, key, fallback);
    }

    /**
     * This function should not be used directly, instead check {@link ModularItemCache#get(ItemStack, String, Object)} for this functionality
     * alternatively {@link ModuleInstance#getFromCache(String, ItemStack, Supplier)} can also be used
     */
    @SuppressWarnings("unchecked")
    public <T> T getFromCache(String key, ItemStack itemStack, Map<String, ModularItemCache.CacheObjectSupplier> supplierMap, Supplier<T> fallback) {
        T data = (T) cachedData.get(key);
        if (data != null) {
            return data;
        }
        ModularItemCache.CacheObjectSupplier cacheSupplier = supplierMap.get(key);
        if (cacheSupplier != null) {
            data = (T) cacheSupplier.apply(itemStack);
            if (data != null) {
                cachedData.put(key, data);
                return data;
            }
        }
        return fallback.get();
    }

    @Override
    public String toString() {
        return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow().toString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true; // Check for object identity first
        if (!(object instanceof ModuleInstance)) return false; // Ensure the object is of type ModuleInstance

        ModuleInstance other = (ModuleInstance) object;

        // Compare the 'module' field
        if (!this.module.equals(other.module)) return false;

        // Compare the 'moduleData' map deeply
        if (!this.moduleData.equals(other.moduleData)) return false;

        // Compare the 'subModules' map deeply
        if (this.subModules.size() != other.subModules.size()) return false; // Check size first for efficiency
        for (Map.Entry<String, ModuleInstance> entry : this.subModules.entrySet()) {
            ModuleInstance otherSubModule = other.subModules.get(entry.getKey());
            if (otherSubModule == null || !entry.getValue().equals(otherSubModule)) {
                return false; // Check if key exists and deeply compare values
            }
        }

        return true; // All checks passed, the objects are deeply equal
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
