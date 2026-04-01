package smartin.miapi.modules;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.base.Material;
import smartin.miapi.modules.cache.DataCache;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.slot.SlotProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Mutable Object to cache things on static record.
 * some stuff is stored natively on here to prevent cache lookups.
 * if you as an addon developer want to cache something, use the cache methods in here.
 * if a faster cache is desired on a ModuleInstance basis mixins are the only way.
 */
public class ModuleInstanceLocalCache {

    //CORE STUFF
    ModuleInstance record;
    ModuleInstance parent;
    ItemStack last = ItemStack.EMPTY;
    volatile ItemModule module;
    volatile List<ModuleInstance> flatList;
    volatile private List<ModuleInstance> sortedChildren;
    private final Object childrenLock = new Object();
    volatile private long lastClear = ModularItemCache.lastClearTimeStamp;

    public ItemModule getModule() {
        confirmTime();
        ItemModule local = module;
        if (local != null) return local;

        synchronized (lock) {
            local = module;
            if (local == null) {
                local = RegistryInventory.ITEM_MODULE_MIAPI_REGISTRY.get(record.moduleId());
                if (local == null) {
                    local = ItemModule.empty;
                    if (!ReloadEvents.isInReload()) {
                        Miapi.LOGGER.warn("could not find module " + record.moduleId() + " substituting with empty module");
                    }
                }
                module = local;
            }
        }
        return local;
    }

    private volatile SequencedMap<String, ModuleInstance> subModuleMap;
    private final Object subModuleLock = new Object();

    public ModuleInstance owner() {
        return record;
    }

    public SequencedMap<String, ModuleInstance> getSubModules() {
        confirmTime();

        SequencedMap<String, ModuleInstance> local = subModuleMap;
        if (local != null) return local;

        synchronized (subModuleLock) {
            local = subModuleMap;
            if (local == null) {
                List<Map.Entry<String, ModuleInstance>> entries =
                        new ArrayList<>(record.children().entrySet());

                // stable sort by priority (lowest first)
                entries.sort(Comparator.comparingDouble(
                        e -> computePriority(e.getKey(), e.getValue())
                ));

                LinkedHashMap<String, ModuleInstance> ordered = new LinkedHashMap<>();
                for (var entry : entries) {
                    ordered.put(entry.getKey(), entry.getValue());
                }

                local = Collections.unmodifiableSequencedMap(ordered);
                subModuleMap = local;
            }
        }
        return local;
    }

    /**
     * only includes direct children.
     */
    public List<ModuleInstance> getSortedChildren() {
        List<ModuleInstance> local = sortedChildren;
        if (local != null) return local;

        synchronized (childrenLock) {
            local = sortedChildren;
            if (local == null) {
                local = List.copyOf(computeSortedChildren());
                sortedChildren = local;
            }
        }
        return local;
    }

    private @NotNull List<ModuleInstance> computeSortedChildren() {
        return new ArrayList<>(getSubModules().values());
    }

    private double computePriority(String slotKey, ModuleInstance value) {
        return SlotProperty.getInstance()
                .getData(value.cache().getPropertiesRaw(true))
                .flatMap(slotMap -> Optional.ofNullable(slotMap.get(slotKey)))
                .map(slot -> slot.priority)
                .orElse(0.0);
    }

    /**
     * this should nolonger be required to be called.
     * caches should auto invalidate themselves.
     */
    public void clear() {
        synchronized (lock) {
            clearInternal();
            lastClear = ModularItemCache.lastClearTimeStamp;
        }
    }

    private void clearInternal() {
        module = null;
        flatList = null;
        sortedChildren = null;
        subModuleMap = null;

        properties = null;
        isFullyInit = false;

        initialized.clear();
        itemMergedProperties.clear();
        cachedData.clear();
        itemStackCache.clear();
    }


    public List<ModuleInstance> allSubModules() {
        if (flatList == null) {
            List<ModuleInstance> nextFlatList = new ArrayList<>();
            Deque<ModuleInstance> queue = new ArrayDeque<>();
            queue.add(record);

            while (!queue.isEmpty()) {
                ModuleInstance module = queue.pollFirst();
                nextFlatList.add(module);
                queue.addAll(module.cache().getSortedChildren());
            }

            flatList = List.copyOf(nextFlatList);
        }
        return flatList;
    }

    /**
     * WARNING uncached only use sparingly,
     * use {@link ModuleInstance#getFlatList()} instead!
     */
    public List<ModuleInstance> allSubUnsortedModules() {
        List<ModuleInstance> nextFlatList = new ArrayList<>();
        Deque<ModuleInstance> queue = new ArrayDeque<>();
        queue.add(record);

        while (!queue.isEmpty()) {
            ModuleInstance module = queue.pollFirst();
            nextFlatList.add(module);
            queue.addAll(module.children().values());
        }

        return List.copyOf(nextFlatList);
    }

    public Optional<ModuleInstance> getParent() {
        return Optional.ofNullable(parent);
    }

    // PROPERTY STUFF
    /**
     * one should *NEVER* access this field, its fundamentally unsave.
     * it is only exposed so the resolver can access it to set it.
     * its data is fundamentally volatile and no guarantees can be given.
     * use {@link ModuleInstanceLocalCache#getPropertiesRaw(boolean)} instead!
     */
    @Nullable
    @ApiStatus.Internal
    public volatile Map<ModuleProperty<?>, Object> properties = null;
    private volatile boolean isFullyInit = false;
    public final Map<ModuleProperty<?>, Object> initialized = new ConcurrentHashMap<>();
    private final Map<ModuleProperty<?>, Object> itemMergedProperties = new ConcurrentHashMap<>();
    public final Object lock = new Object();
    private final Object resolveLock = new Object();

    /**
     * will cause deadlock if called during PropertyResolve!
     * is thread save, will wait upon property solve to finish if property solve is occuring!
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(ModuleProperty<T> property) {
        if (ReloadEvents.isInReload()) {
            synchronized (lock) {
                properties = null;
                initialized.clear();
            }
            return null;
        }

        confirmTime();

        Object cached = initialized.get(property);
        if (cached != null) {
            return (T) cached;
        }
        Map<ModuleProperty<?>, Object> props = getPropertiesRaw(false);
        if (props == null) {
            Miapi.LOGGER.error("property resolve failed!");
            Miapi.LOGGER.error("Could not resolve Property {}", property);
            Miapi.LOGGER.error("for Module {}", record.moduleId());
            return null;
        }

        Object raw = props.get(property);
        if (raw == null) return null;

        if (initialized.containsKey(property)) {
            return (T) initialized.get(property);
        } else {
            T init = property.initialize((T) raw, record);
            initialized.put(property, init);
            return init;
        }
    }

    /**
     * @param duringResolve if set to true, will return partial maps during resolve
     *                      if false, will block until resolve is finished.
     */
    public Map<ModuleProperty<?>, Object> getPropertiesRaw(boolean duringResolve) {
        ModuleInstance root = getRoot();
        ModuleInstanceLocalCache rootCache = root.cache();
        if (!duringResolve) {
            synchronized (rootCache.lock) {
                if (this.properties != null) {
                    return this.properties;
                }
                if (rootCache.properties == null) {
                    PropertyResolver.resolve(root);
                }
            }
        } else {
            synchronized (rootCache.resolveLock) {
                if (this.properties != null) {
                    return this.properties;
                } else {
                    List<ModuleInstance> flatUnsorted = root.cache().allSubUnsortedModules();
                    for (ModuleInstance instance : flatUnsorted) {
                        ModuleInstanceLocalCache cache = instance.cache();
                        cache.properties = new ConcurrentHashMap<>();
                        cache.initialized.clear();
                    }
                }
            }
            synchronized (rootCache.lock) {
                PropertyResolver.resolve(root);
            }
        }
        return this.properties;
    }

    public Map<ModuleProperty<?>, Object> getInitializedProperties() {
        confirmTime();
        if (ReloadEvents.isInReload()) {
            return Map.of();
        }
        if (isFullyInit) {
            return initialized;
        }
        Map<ModuleProperty<?>, Object> props = getPropertiesRaw(false);
        for (var property : props.keySet()) {
            getProperty(property);
        }
        isFullyInit = true;
        return initialized;
    }

    public void confirmStack(ItemStack itemStack) {
        if (this.last != itemStack) {
            this.last = itemStack;
            synchronized (lock) {
                clearInternal();
            }
        }
    }

    public void confirmTime() {
        long global = ModularItemCache.lastClearTimeStamp;
        if (this.lastClear != global) {
            synchronized (lock) {
                if (this.lastClear != global) {
                    clearInternal();
                    this.lastClear = global;
                }
            }
        }
    }

    public ItemStack getStack() {
        return last;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getPropertyItemStack(ModuleProperty<T> property) {
        confirmTime();
        if (itemMergedProperties.containsKey(property)) {
            return (T) itemMergedProperties.get(property);
        }
        synchronized (lock) {
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
        confirmTime();
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
        confirmTime();
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
        confirmTime();
        return ModularItemCache.get(itemStack, key, fallback);
    }

    /**
     * This function should not be used directly, instead check {@link ModularItemCache#get(ItemStack, String, Object)} for this functionality
     * alternatively {@link ModuleInstanceLocalCache#getFromCache(String, ItemStack, Supplier)} can also be used
     */
    @SuppressWarnings("unchecked")
    public <T> T getFromCache(String key, ItemStack itemStack, Map<String, ModularItemCache.CacheObjectSupplier> supplierMap, Supplier<T> fallback) {
        confirmTime();
        confirmStack(itemStack);
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
