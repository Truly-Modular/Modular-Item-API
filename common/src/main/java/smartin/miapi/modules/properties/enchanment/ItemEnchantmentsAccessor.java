package smartin.miapi.modules.properties.enchanment;

import net.minecraft.world.item.ItemStack;

public interface ItemEnchantmentsAccessor {
    ItemStack getOwnerStack();

    void setOwnerStack(ItemStack itemStack);
}

