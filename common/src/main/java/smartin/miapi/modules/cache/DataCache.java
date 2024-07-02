package smartin.miapi.modules.cache;

import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.ModuleInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class DataCache {
    public static Map<String, ItemCacheSupplier> ITEM_CACHE_SUPPLIER = new HashMap<>();
    public static Map<String, ModuleCacheSupplier> MODULE_CACHE_SUPPLIER = new HashMap<>();

    Map<String, Object> cacheContents = new ConcurrentHashMap<>();

    public <T> T get(String key, Supplier<T> fallback) {
        return (T) cacheContents.computeIfAbsent(key, (s) -> fallback.get());
    }

    public static <T, B> T get(Map<String, Object> cacheContents, Map<String, Function<B, T>> supplier, B context, String key, Supplier<T> fallback) {
        if (cacheContents.containsKey(key)) {
            return (T) cacheContents.get(key);
        } else {
            T result = supplier.get(key).apply(context);
            if (result == null) {
                return fallback.get();
            }
            return result;
        }
    }

    public interface ItemCacheSupplier extends Function<ItemStack, Object> {
        @Override
        Object apply(ItemStack stack);
    }

    public interface ModuleCacheSupplier extends Function<ModuleInstance, Object> {
        @Override
        Object apply(ModuleInstance stack);
    }
}
