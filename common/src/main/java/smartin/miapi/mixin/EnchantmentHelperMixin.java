package smartin.miapi.mixin;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.AnvilScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.EnchantmentProperty;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static smartin.miapi.modules.properties.AttributeProperty.getAttributeModifiersForSlot;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @Inject(method = "Lnet/minecraft/enchantment/EnchantmentHelper;getPossibleEntries(ILnet/minecraft/item/ItemStack;Z)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private static void modifyAttributeModifiers(int power, ItemStack stack, boolean treasureAllowed, CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        if (stack.getItem() instanceof ModularItem) {
            List<EnchantmentLevelEntry> enchantments = new ArrayList<>();

            Iterator<Enchantment> iterator = Registries.ENCHANTMENT.iterator();
            while (iterator.hasNext()) {
                Enchantment enchantment = iterator.next();

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
}
