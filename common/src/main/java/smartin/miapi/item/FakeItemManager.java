package smartin.miapi.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.datapack.ReloadEvents;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FakeItemManager {
    private static ThreadLocal<Map<Item, ItemStack>> SHORT_CACHE = ThreadLocal.withInitial(HashMap::new);
    private static final Map<Item, ItemStack> LONG_CACHE = new ConcurrentHashMap<>();

    static {
        ReloadEvents.START.subscribe((isClient, registryAccess) -> {
            SHORT_CACHE = ThreadLocal.withInitial(HashMap::new);
            LONG_CACHE.clear();
        });
    }

    public static void getItemCall(ItemStack itemStack, Item item) {
        SHORT_CACHE.get().put(item, itemStack);
        LONG_CACHE.put(item, itemStack);
    }

    public static ItemStack getDefaultInstance(Item item) {
        return SHORT_CACHE.get().getOrDefault(item, LONG_CACHE.getOrDefault(item, new ItemStack(item))).copy();
    }

    @Nullable
    public static ItemStack getLastInstance(Item item) {
        return SHORT_CACHE.get().getOrDefault(item, LONG_CACHE.get(item));
    }
}
