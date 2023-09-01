package smartin.miapi.mixin;

import com.google.common.collect.Lists;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.ChannelingProperty;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = EnchantmentHelper.class, priority = 700)
public class EnchantmentHelperMixin {

    @Inject(method = "Lnet/minecraft/enchantment/EnchantmentHelper;getPossibleEntries(ILnet/minecraft/item/ItemStack;Z)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private static void miapi$modifyAttributeModifiers(int power, ItemStack stack, boolean treasureAllowed, CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        if (stack.getItem() instanceof ModularItem) {
            List<EnchantmentLevelEntry> enchantments = new ArrayList<>();

            for (Enchantment enchantment : Registries.ENCHANTMENT) {
                if (enchantment.isTreasure() && !treasureAllowed) {
                    continue;
                }

                if (!enchantment.isAvailableForRandomSelection()) {
                    continue;
                }

                boolean acceptableItem = enchantment.isAcceptableItem(stack);

                if (acceptableItem) {
                    continue;
                }

                for (int level = enchantment.getMaxLevel(); level >= enchantment.getMinLevel(); level--) {
                    int minPower = enchantment.getMinPower(level);
                    int maxPower = enchantment.getMaxPower(level);

                    if (power >= minPower && power <= maxPower) {
                        enchantments.add(new EnchantmentLevelEntry(enchantment, level));
                        break;
                    }
                }
            }
            cir.setReturnValue(enchantments);
        }
    }

    @Inject(method = "Lnet/minecraft/enchantment/EnchantmentHelper;hasChanneling(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private static void miapi$modifyChannelling(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof ModularItem) {
            if (ChannelingProperty.hasChanneling(stack)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "Lnet/minecraft/enchantment/EnchantmentHelper;generateEnchantments(Lnet/minecraft/util/math/random/Random;Lnet/minecraft/item/ItemStack;IZ)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private static void miapi$ModifyPossibleEntries(Random random, ItemStack stack, int level, boolean treasureAllowed, CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        Miapi.LOGGER.info("Modular Item Enchanting check - 1");
    }

    @Inject(method = "Lnet/minecraft/enchantment/EnchantmentHelper;getPossibleEntries(ILnet/minecraft/item/ItemStack;Z)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private static void miapi$ModifyPossibleEntries(int power, ItemStack stack, boolean treasureAllowed, CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        Miapi.LOGGER.info("Modular Item Enchanting check - 2");
        if(stack.getItem() instanceof ModularItem){
            Miapi.LOGGER.info("Modular Item Enchanting check - 3");
            ArrayList<EnchantmentLevelEntry> list = Lists.newArrayList();
            for (Enchantment enchantment : Registries.ENCHANTMENT) {
                if ( (!enchantment.isTreasure() || enchantment.isTreasure() && treasureAllowed) &&
                        enchantment.isAvailableForRandomSelection() && enchantment.isAcceptableItem(stack))
                {
                    for (int i = enchantment.getMaxLevel(); i > enchantment.getMinLevel() - 1; --i) {
                        if (power < enchantment.getMinPower(i) || power > enchantment.getMaxPower(i)) continue;
                        list.add(new EnchantmentLevelEntry(enchantment, i));
                    }
                }
            }
            cir.setReturnValue(list);
        }
    }
}
