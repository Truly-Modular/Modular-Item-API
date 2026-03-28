package smartin.miapi.modules;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.base.Material;
import smartin.miapi.modules.cache.DataCache;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.DisplayNameProperty;
import smartin.miapi.modules.properties.slot.SlotProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Mutable Object to cache things on static record.
 */
public class ModuleInstanceLocalCache {

    //CORE STUFF
    ModuleInstance record;
    ModuleInstance parent;
    ItemStack last = ItemStack.EMPTY;
    ItemModule module;
    List<ModuleInstance> flatList;
    private List<ModuleInstance> sortedChildren;
    private final Object childrenLock = new Object();

    public ItemModule getModule() {
        if (module == null) {
            this.module = RegistryInventory.ITEM_MODULE_MIAPI_REGISTRY.get(record.moduleId());
            if (this.module == null) {
                this.module = ItemModule.empty;
                if (ReloadEvents.isInReload()) {
                    return module;
                }
                Miapi.LOGGER.warn("could not find module " + record.moduleId() + " substituting with empty module");
            }
        }
        return module;
    }

    private SequencedMap<String, ModuleInstance> subModuleMap;
    private final Object subModuleLock = new Object();

    public ModuleInstance owner() {
        return record;
    }

    public SequencedMap<String, ModuleInstance> getSubModules() {
        if (subModuleMap != null) {
            return subModuleMap;
        }

        synchronized (subModuleLock) {
            if (subModuleMap != null) {
                return subModuleMap;
            }

            SequencedMap<String, ModuleInstance> map = new LinkedHashMap<>();

            if (record.children() != null) {
                map.putAll(record.children());
            }

            subModuleMap = map;
            return subModuleMap;
        }
    }

    public List<ModuleInstance> getSortedChildren() {
        if (sortedChildren != null) {
            return sortedChildren;
        }

        synchronized (childrenLock) {
            if (sortedChildren != null) {
                return sortedChildren;
            }

            List<ModuleInstance> list = new ArrayList<>();

            Map<String, ModuleInstance> children = record.children();
            if (children != null) {
                for (var entry : children.entrySet()) {
                    list.add(entry.getValue());
                }
            }

            // Sort using Child priority
            list.sort((a, b) -> {
                double pa = computePriority(a);
                double pb = computePriority(b);
                return Double.compare(pb, pa); // descending (higher priority first)
            });

            sortedChildren = List.copyOf(list);
            return sortedChildren;
        }
    }

    private double computePriority(ModuleInstance value) {
        return SlotProperty.getInstance()
                .getData(value)
                .map(slot -> {
                    // slot id is derived from map key, not directly available here
                    // fallback to 0 if not resolvable
                    return 0.0;
                })
                .orElse(0.0);
    }

    public void clear() {
        synchronized (lock) {
            module = null;
            flatList = null;
            sortedChildren = null;

            properties = null;
            initialized.clear();

            cachedData.clear();
            itemStackCache.clear();
        }
    }


    List<ModuleInstance> allSubModules() {
        if (flatList == null) {
            List<ModuleInstance> nextFlatList = new ArrayList<>();
            List<ModuleInstance> queue = new ArrayList<>();
            queue.add(record);

            while (!queue.isEmpty()) {
                ModuleInstance module = queue.removeFirst();
                if (module != null) {
                    nextFlatList.add(module);

                    // use sorted children from cache
                    List<ModuleInstance> children = module.cache().getSortedChildren();
                    queue.addAll(0, children);
                }
            }

            flatList = nextFlatList;
        }
        return flatList;
    }

    public Optional<ModuleInstance> getParent() {
        return Optional.ofNullable(parent);
    }

    // PROPERTY STUFF
    @Nullable
    @ApiStatus.Internal
    public Map<ModuleProperty<?>, Object> properties = null;
    private boolean isFullyInit = false;
    private final Map<ModuleProperty<?>, Object> initialized = new ConcurrentHashMap<>();
    private final Map<ModuleProperty<?>, Object> itemMergedProperties = new ConcurrentHashMap<>();
    private final Object lock = new Object();

    @SuppressWarnings("unchecked")
    public <T> T getProperty(ModuleProperty<T> property) {
        if (ReloadEvents.isInReload()) {
            properties = null;
            initialized.clear();
            return null;
        }

        Object cached = initialized.get(property);
        if (cached != null) {
            return (T) cached;
        }

        if (properties == null) {
            PropertyResolver.resolve(record);
        }

        if (properties == null) {
            Miapi.LOGGER.error("property resolve failed!");
            Miapi.LOGGER.error("Could not resolve Property " + property);
            Miapi.LOGGER.error("for Module " + record.moduleId());
            return null;
        }

        Object raw = properties.get(property);
        if (raw == null) return null;

        T value = (T) raw;
        value = property.initialize(value, record);

        initialized.put(property, value);
        return value;
    }

    /**
     * WARNING! do only use if otherwise would cause a stack overflow
     * @return
     */
    public Map<ModuleProperty<?>, Object> getPropertiesRaw() {
        return properties;
    }

    @SuppressWarnings("unchecked")
    public Map<ModuleProperty<?>, Object> getInitializedProperties() {
        if (ReloadEvents.isInReload()) {
            return Map.of();
        }
        if (isFullyInit) {
            return initialized;
        }
        getProperty(DisplayNameProperty.property);
        for (var property : properties.keySet()) {
            getProperty(property);
        }
        isFullyInit = true;
        return initialized;
    }

    public void confirmStack(ItemStack itemStack) {
        if (this.last != itemStack) {
            this.last = itemStack;
            clear();
        }
    }

    public ItemStack getStack() {
        return last;
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
        for (ModuleInstance moduleInstance : root.getFlatList()) {
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

    public ModuleInstance getRoot() {
        return this.getParent().map(p -> p.cache().getRoot()).orElse(record);
    }

    @Environment(EnvType.CLIENT)
    public Component getModuleName() {
        String moduleName = getModule().id().toString();
        moduleName = moduleName.replace(":", ".");
        moduleName = moduleName.replaceAll("/", ".");
        Material material = MaterialProperty.getMaterial(this.record);
        if (material != null) {
            return Component.translatable(Miapi.MOD_ID + ".module." + moduleName, material.getTranslation());
        }
        return StatResolver.translateAndResolve(Miapi.MOD_ID + ".module." + moduleName, this.record);
    }

    @Environment(EnvType.CLIENT)
    public Component getModuleDescription() {
        String moduleName = getModule().id().toString();
        moduleName = moduleName.replace(":", ".");
        moduleName = moduleName.replaceAll("/", ".");
        Material material = MaterialProperty.getMaterial(this.record);
        if (material != null) {
            return Component.translatable(Miapi.MOD_ID + ".module." + moduleName + ".description", material.getTranslation());
        }
        return StatResolver.translateAndResolve(Miapi.MOD_ID + ".module." + moduleName + ".description", this.record);
    }


    public Map<String, Object> cachedData = new ConcurrentHashMap<>();
    public Map<String, Object> itemStackCache = new ConcurrentHashMap<>();

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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
}
