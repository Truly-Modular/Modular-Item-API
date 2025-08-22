package smartin.miapi.modules.cache;

import dev.architectury.event.EventResult;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModularItemCache {
    protected static Map<String, CacheObjectSupplier> supplierMap = new ConcurrentHashMap<>();
    public static Map<String, DataCache.ModuleCacheSupplier> MODULE_CACHE_SUPPLIER = new ConcurrentHashMap<>();
    public static ConcurrentWeakInstanceTracker<ModuleInstance> modules = new ConcurrentWeakInstanceTracker<>();

    public static void setSupplier(String key, CacheObjectSupplier supplier) {
        supplierMap.put(key, supplier);
    }

    static {
        MiapiEvents.CLEAR_CACHE.register(new MiapiEvents.EmptyEvent() {
            @Override
            public EventResult onReload() {
                discardCache();
                return EventResult.pass();
            }
        });
    }

    @Nullable
    public static <T> T getRaw(ItemStack stack, String key) {
        return get(stack, key, (T) null);
    }

    public static <T> T get(ItemStack stack, String key, T fallback) {
        return get(stack, key, () -> fallback);
    }

    public static <T> T get(ItemStack stack, String key, Supplier<T> fallback) {
        ModuleInstance moduleInstance = ItemModule.getModules(stack);
        if (moduleInstance != null) {
            return moduleInstance.getFromCache(key, stack, supplierMap, fallback);
        }
        return fallback.get();
    }

    public static void clear(ItemStack stack, String key) {
        ModuleInstance moduleInstance = ItemModule.getModules(stack);
        if (moduleInstance != null) {
            moduleInstance.cachedData.remove(key);
        }
    }

    public static <T> T getVisualOnlyCache(ItemStack stack, String key, T fallback) {
        if (!ReloadEvents.isInReload() && !stack.isEmpty() && VisualModularItem.isVisualModularItem(stack)) {
            return get(stack, key, () -> fallback);
        }
        return fallback;
    }

    public static void discardCache() {
        modules.getInstances().forEach((m) -> {
            if (m != null) {
                m.clearCaches();
                if (m.parent == null && m.contextStack != null) {
                    m.writeToItem(m.contextStack);
                }
            }
        });
    }


    public interface CacheObjectSupplier extends Function<ItemStack, Object> {
        @Override
        Object apply(ItemStack stack);
    }


    public static class ConcurrentWeakInstanceTracker<T> {
        private final ReferenceQueue<T> refQueue = new ReferenceQueue<>();
        private final ConcurrentHashMap<WeakReference<T>, Boolean> instanceMap = new ConcurrentHashMap<>();

        public void addInstance(T instance) {
            cleanup(); // periodically clean stale references
            WeakReference<T> ref = new WeakReference<>(instance, refQueue);
            instanceMap.put(ref, Boolean.TRUE);
        }

        public Set<T> getInstances() {
            cleanup();
            Set<T> result = new HashSet<>();
            for (WeakReference<T> ref : instanceMap.keySet()) {
                T obj = ref.get();
                if (obj != null) {
                    result.add(obj);
                }
            }
            return result;
        }

        private void cleanup() {
            WeakReference<? extends T> ref;
            while ((ref = (WeakReference<? extends T>) refQueue.poll()) != null) {
                instanceMap.remove(ref);
            }
        }
    }
}
