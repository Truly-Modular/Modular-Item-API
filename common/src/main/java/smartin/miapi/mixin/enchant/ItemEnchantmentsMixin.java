package smartin.miapi.mixin.enchant;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import smartin.miapi.modules.properties.enchanment.ItemEnchantmentsAccessor;

@Mixin(ItemEnchantments.class)
public abstract class ItemEnchantmentsMixin implements ItemEnchantmentsAccessor {

    @Unique
    ItemStack ownerStack;

    @Override
    public ItemStack getOwnerStack() {
        return ownerStack;
    }

    @Override
    public void setOwnerStack(ItemStack itemStack) {
        this.ownerStack = itemStack;
    }
}
