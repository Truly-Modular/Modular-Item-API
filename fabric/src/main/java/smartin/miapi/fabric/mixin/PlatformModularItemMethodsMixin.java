package smartin.miapi.fabric.mixin;

import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import smartin.miapi.item.modular.PlatformModularItemMethods;
import smartin.miapi.modules.properties.enchanment.AllowedEnchantments;

/**
 * This class overwrites the FabricItem methods for Modular items.
 */
@Mixin(PlatformModularItemMethods.class)
public interface PlatformModularItemMethodsMixin extends FabricItem {

    @Override
    default boolean canBeEnchantedWith(ItemStack stack, Holder<Enchantment> enchantment, EnchantingContext context) {
        return AllowedEnchantments.isAllowed(stack, enchantment, context == EnchantingContext.PRIMARY
                ? enchantment.value().isPrimaryItem(stack)
                : enchantment.value().canEnchant(stack));
    }
}
