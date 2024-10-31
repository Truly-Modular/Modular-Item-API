package smartin.miapi.mixin;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Util;
import net.minecraft.util.collection.Weighting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.FakeEnchantment;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.ChannelingProperty;
import smartin.miapi.modules.properties.EnchantAbilityProperty;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = EnchantmentHelper.class, priority = 700)
public class EnchantmentHelperMixin {

    @ModifyReturnValue(method = "getPossibleEntries(ILnet/minecraft/item/ItemStack;Z)Ljava/util/List;", at = @At("RETURN"))
    private static List<EnchantmentLevelEntry> miapi$modifyAttributeModifiers(List<EnchantmentLevelEntry> original, int power, ItemStack stack, boolean treasureAllowed) {
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
            return enchantments;
        }
        return original;
    }

    @ModifyReturnValue(method = "hasChanneling(Lnet/minecraft/item/ItemStack;)Z", at = @At("RETURN"))
    private static boolean miapi$modifyChannelling(boolean original, ItemStack stack) {
        if (stack.getItem() instanceof ModularItem) {
            if (ChannelingProperty.hasChanneling(stack)) {
                return true;
            }
        }
        return original;
    }

    @Inject(method = "onTargetDamaged(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private static void miapi$addMagicDamage(LivingEntity attacker, Entity target, CallbackInfo ci) {
        if (target instanceof LivingEntity defender) {
            MiapiEvents.LIVING_ATTACK.invoker().attack(attacker, defender);
        }
    }

    @ModifyReturnValue(
            method = "generateEnchantments(Lnet/minecraft/util/math/random/Random;Lnet/minecraft/item/ItemStack;IZ)Ljava/util/List;",
            at = @At("RETURN"))
    private static List<EnchantmentLevelEntry> miapi$modifyGenerateEnchantments(List<EnchantmentLevelEntry> original, Random random, ItemStack stack, int level, boolean treasureAllowed) {
        if (stack.getItem() instanceof ModularItem) {
            ArrayList<EnchantmentLevelEntry> list = Lists.newArrayList();
            int i = (int) Math.ceil(EnchantAbilityProperty.getEnchantAbility(stack));
            if (i <= 0) {
                return list;
            }
            level += 1 + random.nextInt(i / 4 + 1) + random.nextInt(i / 4 + 1);
            float f = (random.nextFloat() + random.nextFloat() - 1.0f) * 0.15f;
            List<EnchantmentLevelEntry> list2 = getLevels(level = MathHelper.clamp(Math.round((float) level + (float) level * f), 1, Integer.MAX_VALUE), stack, treasureAllowed);
            if (!list2.isEmpty()) {
                Weighting.getRandom(random, list2).ifPresent(list::add);
                while (random.nextInt(50) <= level) {
                    if (!list.isEmpty()) {
                        EnchantmentHelper.removeConflicts(list2, Util.getLast(list));
                    }
                    if (list2.isEmpty()) break;
                    Weighting.getRandom(random, list2).ifPresent(list::add);
                    level /= 2;
                }
            }
            return list;
        }
        return original;
    }

    @ModifyReturnValue(
            method = "getPossibleEntries(ILnet/minecraft/item/ItemStack;Z)Ljava/util/List;",
            at = @At("RETURN"))
    private static List<EnchantmentLevelEntry> miapi$modifyPossibleEntries(List<EnchantmentLevelEntry> original, int power, ItemStack stack, boolean treasureAllowed) {
        if (stack.getItem() instanceof ModularItem) {
            ArrayList<EnchantmentLevelEntry> list = Lists.newArrayList();
            for (Enchantment enchantment : Registries.ENCHANTMENT) {
                if (
                        (!enchantment.isTreasure() || enchantment.isTreasure() && treasureAllowed) &&
                        enchantment.isAvailableForRandomSelection() && enchantment.isAcceptableItem(stack)) {
                    for (int i = enchantment.getMaxLevel(); i > enchantment.getMinLevel() - 1; --i) {
                        if (power < enchantment.getMinPower(i) || power > enchantment.getMaxPower(i)) continue;
                        list.add(new EnchantmentLevelEntry(enchantment, i));
                    }
                }
            }
            return list;
        }
        return original;
    }

    @ModifyReturnValue(
            method = "getLevel(Lnet/minecraft/enchantment/Enchantment;Lnet/minecraft/item/ItemStack;)I",
            at = @At("RETURN")
    )
    private static int miapi$modifyPossibleEntries(int original, Enchantment enchantment, ItemStack stack) {
        if (stack.getItem() instanceof ModularItem) {
            return FakeEnchantment.getFakeLevel(enchantment, stack, original);
        }
        return original;
    }

    private static List<EnchantmentLevelEntry> getLevels(int power, ItemStack stack, boolean treasureAllowed) {
        ArrayList<EnchantmentLevelEntry> list = Lists.newArrayList();
        if (stack.getItem() instanceof ModularItem) {
            for (Enchantment enchantment : Registries.ENCHANTMENT) {
                if (
                        (!enchantment.isTreasure() || enchantment.isTreasure() && treasureAllowed) &&
                        enchantment.isAvailableForRandomSelection() && enchantment.isAcceptableItem(stack)) {
                    for (int i = enchantment.getMaxLevel(); i > enchantment.getMinLevel() - 1; --i) {
                        if (power < enchantment.getMinPower(i) || power > enchantment.getMaxPower(i)) continue;
                        list.add(new EnchantmentLevelEntry(enchantment, i));
                    }
                }
            }
        }
        return list;
    }

    @Inject(
            method = "forEachEnchantment(Lnet/minecraft/enchantment/EnchantmentHelper$Consumer;Lnet/minecraft/item/ItemStack;)V",
            at = @At("TAIL"),
            cancellable = true)
    private static void miapi$addFakeEnchants(EnchantmentHelper.Consumer consumer, ItemStack stack, CallbackInfo ci) {
        FakeEnchantment.addEnchantments(consumer, stack);
    }
}
