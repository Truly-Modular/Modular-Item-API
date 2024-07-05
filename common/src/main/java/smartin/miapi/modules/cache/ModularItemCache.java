package smartin.miapi.modules.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModularItemCache {
    protected static Map<String, CacheObjectSupplier> supplierMap = new ConcurrentHashMap<>();
    public static Map<String, DataCache.ModuleCacheSupplier> MODULE_CACHE_SUPPLIER = new ConcurrentHashMap<>();

    public static void setSupplier(String key, CacheObjectSupplier supplier) {
        supplierMap.put(key, supplier);
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
        if (moduleInstance == null) {
            return moduleInstance.getFromCache(key, stack, supplierMap, fallback);
        }
        return fallback.get();
    }

    public static <T> T getVisualOnlyCache(ItemStack stack, String key, T fallback) {
        if (!ReloadEvents.isInReload() && !stack.isEmpty() && stack.getItem() instanceof VisualModularItem) {
            return get(stack, key, () -> fallback);
        }
        return fallback;
    }

    public static void discardCache() {

    }


    public interface CacheObjectSupplier extends Function<ItemStack, Object> {
        @Override
        Object apply(ItemStack stack);
    }
}
