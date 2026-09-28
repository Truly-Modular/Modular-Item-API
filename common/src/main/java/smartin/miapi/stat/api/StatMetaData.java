package smartin.miapi.stat.api;

import net.minecraft.world.item.ItemStack;

public interface StatMetaData<T> {
    default boolean hasStat(ItemStack stack) {
        return getData(stack) != null;
    }

    T getData(ItemStack stack);

    default boolean shouldBeVisible(ItemStack base, ItemStack compare) {
        return hasStat(base) || hasStat(compare);
    }

    default double getPriority() {
        return 0;
    }
}