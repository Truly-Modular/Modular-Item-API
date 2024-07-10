package smartin.miapi.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class FakeItemstackReferenceProvider {

    public static Map<Item, ItemStack> weakCache = Collections.synchronizedMap(new WeakHashMap<>());

    public static void setReference(Item item, ItemStack itemStack) {
        weakCache.put(item, itemStack);
    }

    @Nullable
    public static ItemStack getFakeReference(Item item) {
        return weakCache.get(item);
    }
}
