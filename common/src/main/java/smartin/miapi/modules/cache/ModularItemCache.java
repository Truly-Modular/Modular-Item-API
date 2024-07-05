package smartin.miapi.modules.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Environment;
import smartin.miapi.client.atlas.MaterialSpriteManager;
import smartin.miapi.client.model.ModelTransformer;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.VisualModularItem;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static smartin.miapi.events.MiapiEvents.CACHE_CLEAR_EVENT;

public class ModularItemCache {
    protected static Map<String, CacheObjectSupplier> supplierMap = new HashMap<>();
    public static final long CACHE_SIZE = 1000;
    public static final long CACHE_LIFETIME = 2;
    public static final TimeUnit CACHE_LIFETIME_UNIT = TimeUnit.MINUTES;
    protected static final LoadingCache<ItemStack, Cache> cache = CacheBuilder.newBuilder()
            .maximumSize(CACHE_SIZE)
            .expireAfterAccess(CACHE_LIFETIME, CACHE_LIFETIME_UNIT)
            .build(new CacheLoader<>() {
                public @NotNull Cache load(@NotNull ItemStack key) {
                    return new Cache(key);
                }
            });

    public static void setSupplier(String key, CacheObjectSupplier supplier) {
        supplierMap.put(key, supplier);
    }

    @Nullable
    public static <T> T getRaw(ItemStack stack, String key) {
        if (!ReloadEvents.isInReload() && !stack.isEmpty() && stack.getItem() instanceof VisualModularItem) {
            Cache itemCache = find(stack);
            return (T) itemCache.get(key);
        }
        return null;
    }

    public static <T> T get(ItemStack stack, String key, T fallback) {
        if (!ReloadEvents.isInReload() && !stack.isEmpty() && stack.getItem() instanceof ModularItem) {
            Cache itemCache = find(stack);
            T object = (T) itemCache.get(key);
            if (object == null) {
                return fallback;
            }
            return object;
        }
        return fallback;
    }

    public static <T> T getVisualOnlyCache(ItemStack stack, String key, T fallback) {
        if (!ReloadEvents.isInReload() && !stack.isEmpty() && stack.getItem() instanceof VisualModularItem) {
            Cache itemCache = find(stack);
            T object = (T) itemCache.get(key);
            if (object == null) {
                return fallback;
            }
            return object;
        }
        return fallback;
    }

    public static void discardCache() {
        CACHE_CLEAR_EVENT.invoker().onReload(Environment.isClient());
        cache.cleanUp();
        cache.invalidateAll();
        if (Environment.isClient()) {
            ModelTransformer.clearCaches();
            MaterialSpriteManager.clear();
        }
    }

    public static void clearUUIDFor(ItemStack stack) {
        if (stack.getItem() instanceof VisualModularItem && stack.hasNbt()) {
            cache.invalidate(stack);
        }
    }

    protected static Cache find(ItemStack stack) {
        try {
            Cache cacheEntry = cache.get(stack, () -> new Cache(stack));
            if (cacheEntry.isValid(stack)) {
                return cacheEntry;
            } else {
                cache.invalidate(stack);
                return cache.get(stack, () -> new Cache(stack));
            }
        } catch (ExecutionException ignored) {
            Cache cache1 = new Cache(stack);
            cache.put(stack, cache1);
            return cache1;
        }
    }

    public interface CacheObjectSupplier extends Function<ItemStack, Object> {
        @Override
        Object apply(ItemStack stack);
    }

    protected static class Cache {
        protected Map<String, Object> map = new ConcurrentHashMap<>();
        public ItemStack stack;

        public Cache(ItemStack stack) {
            this.stack = stack;
        }

        public boolean isValid(ItemStack itemStack) {
            return true;
            //return nbtCompound.equals(itemStack.getNbt());
        }

        public void set(String key, Object object) {
            map.put(key, object);
        }

        public Object get(String key) {
            if (map.containsKey(key)) {
                return map.get(key);
            } else {
                CacheObjectSupplier supplier = supplierMap.get(key);
                if (supplier != null) {
                    Object cached = supplier.apply(stack);
                    if (cached != null) {
                        map.put(key, cached);
                    }
                    return cached;
                }
            }
            return null;
        }
    }
}
