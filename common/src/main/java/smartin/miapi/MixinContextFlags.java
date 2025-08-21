package smartin.miapi;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class MixinContextFlags {
    public static final ThreadLocal<Boolean> CALLED_FROM_MUTABLE = ThreadLocal.withInitial(() -> false);
    public static final ThreadLocal<Map<ItemStack, Item>> IGNORE_NEXT_GET_ITEM_CALL = ThreadLocal.withInitial(HashMap::new);
}

