package smartin.miapi.modules.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.ModularItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class ModularItemCache {
    protected static Map<String, CacheObjectSupplier> supplierMap = new HashMap<>();
    protected static Map<ItemStack, UUID> lookUpTable = new WeakHashMap<>();
    public static final long CACHE_LIFETIME = 2;
    protected static final LoadingCache<UUID, Cache> cache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(CACHE_LIFETIME, TimeUnit.MINUTES)
            .build(new CacheLoader<>() {
                public @NotNull Cache load(@NotNull UUID key) {
                    return new Cache(key, ItemStack.EMPTY);
                }
            });
    protected static final LoadingCache<NbtCompound, UUID> nbtCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(CACHE_LIFETIME, TimeUnit.MINUTES)
            .build(new CacheLoader<>() {
                public @NotNull UUID load(@NotNull NbtCompound key) {
                    return ModularItemCache.getMissingUUID();
                }
            });

    public static void setSupplier(String key, CacheObjectSupplier supplier) {
        supplierMap.put(key, supplier);
    }

    public static Object get(ItemStack stack, String key) {
        if (stack.getItem() instanceof ModularItem) {
            Cache itemCache = find(stack);
            return itemCache.get(key);
        }
        return null;
    }

    public static void discardCache() {
        cache.cleanUp();
        cache.invalidateAll();
    }

    @Nullable
    public static UUID getUUIDFor(ItemStack stack) {
        try {
            return nbtCache.get(stack.getOrCreateNbt());
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setUUIDFor(ItemStack stack, UUID uuid) {
        if (stack.getItem() instanceof ModularItem) {
            if (stack.hasNbt()) {
                nbtCache.put(stack.getOrCreateNbt().copy(), uuid);
            }
        } else {
            Miapi.LOGGER.error("this shouldnt not be called");
        }
    }

    public static void clearUUIDFor(ItemStack stack) {
        if (stack.getItem() instanceof ModularItem) {
            nbtCache.invalidate(stack.getOrCreateNbt());
        }
    }

    protected static Cache find(ItemStack stack) {
        UUID lookUpUUId = lookUpTable.get(stack);
        if (lookUpUUId == null) {
            lookUpUUId = getUUIDFor(stack);
        }
        if (lookUpUUId == null) {
            lookUpUUId = getMissingUUID();
        }
        UUID uuid = lookUpUUId;
        try {
            return cache.get(lookUpUUId, () -> new Cache(uuid, stack));
        } catch (ExecutionException ignored) {
            UUID uuid1 = getMissingUUID();
            Cache cache1 = new Cache(uuid1, stack);
            cache.put(uuid1, cache1);
            return cache1;
        }
    }

    protected static UUID getMissingUUID() {
        UUID uuid;
        do {
            uuid = UUID.randomUUID();
        } while (cache.getIfPresent(uuid) != null);
        return uuid;
    }

    public interface CacheObjectSupplier extends Function<ItemStack, Object> {
        @Override
        Object apply(ItemStack stack);
    }

    protected static class Cache {
        protected Map<String, Object> map = new ConcurrentHashMap<>();
        public UUID uuid;
        public ItemStack stack;
        public int nbtHash;

        public Cache(UUID uuid, ItemStack stack) {
            Miapi.LOGGER.warn("new Cache " + stack.getTranslationKey());
            this.uuid = uuid;
            setUUIDFor(stack, uuid);
            this.stack = stack;
            if (stack.hasNbt()) {
                nbtHash = stack.getOrCreateNbt().hashCode();
            }
        }

        public void set(String key, Object object) {
            map.put(key, object);
        }

        public Object get(String key) {
            return map.computeIfAbsent(key, (id) -> {
                CacheObjectSupplier supplier = supplierMap.get(id);
                if (supplier != null) {
                    return supplier.apply(stack);
                }
                return null;
            });
        }
    }
}
