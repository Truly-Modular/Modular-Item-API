package smartin.miapi.mixin;

import com.google.common.collect.Lists;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.FakeEnchantment;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.ChannelingProperty;
import smartin.miapi.modules.properties.enchanment.EnchantAbilityProperty;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

@Mixin(value = EnchantmentHelper.class, priority = 700)
public class EnchantmentHelperMixin {

    @Inject(method = "getPossibleEntries(ILnet/minecraft/item/ItemStack;Z)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private static void miapi$modifyAttributeModifiers(int power, ItemStack stack, boolean treasureAllowed, CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
        if (stack.getItem() instanceof ModularItem) {
            List<EnchantmentInstance> enchantments = new ArrayList<>();

            for (Enchantment enchantment : Registries.ENCHANTMENT) {
                if (enchantment.isTreasure() && !treasureAllowed) {
                    continue;
                }

                if (!enchantment.isAvailableForRandomSelection()) {
                    continue;
                }

                boolean acceptableItem = enchantment.canEnchant(stack);

                if (acceptableItem) {
                    continue;
                }

                for (int level = enchantment.getMaxLevel(); level >= enchantment.getMinLevel(); level--) {
                    int minPower = enchantment.getMinCost(level);
                    int maxPower = enchantment.getMaxCost(level);

                    if (power >= minPower && power <= maxPower) {
                        enchantments.add(new EnchantmentInstance(enchantment, level));
                        break;
                    }
                }
            }
            cir.setReturnValue(enchantments);
        }
    }

    @Inject(method = "hasChanneling(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private static void miapi$modifyChannelling(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof ModularItem) {
            if (ChannelingProperty.hasChanneling(stack)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "onTargetDamaged(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private static void miapi$addMagicDamage(LivingEntity attacker, Entity target, CallbackInfo ci) {
        if (target instanceof LivingEntity defender) {
            MiapiEvents.LIVING_ATTACK.invoker().attack(attacker, defender);
        }
    }

    @Inject(method = "generateEnchantments(Lnet/minecraft/util/math/random/Random;Lnet/minecraft/item/ItemStack;IZ)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private static void miapi$modifyGenerateEnchantments(RandomSource random, ItemStack stack, int level, boolean treasureAllowed, CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
        if (stack.getItem() instanceof ModularItem) {
            ArrayList<EnchantmentInstance> list = Lists.newArrayList();
            int i = (int) Math.ceil(EnchantAbilityProperty.getEnchantAbility(stack));
            if (i <= 0) {
                cir.setReturnValue(list);
            }
            level += 1 + random.nextInt(i / 4 + 1) + random.nextInt(i / 4 + 1);
            float f = (random.nextFloat() + random.nextFloat() - 1.0f) * 0.15f;
            List<EnchantmentInstance> list2 = getLevels(level = Mth.clamp(Math.round((float) level + (float) level * f), 1, Integer.MAX_VALUE), stack, treasureAllowed);
            if (!list2.isEmpty()) {
                WeightedRandom.getRandomItem(random, list2).ifPresent(list::add);
                while (random.nextInt(50) <= level) {
                    if (!list.isEmpty()) {
                        EnchantmentHelper.filterCompatibleEnchantments(list2, Util.lastOf(list));
                    }
                    if (list2.isEmpty()) break;
                    WeightedRandom.getRandomItem(random, list2).ifPresent(list::add);
                    level /= 2;
                }
            }
            cir.setReturnValue(list);
        }
    }

    @Inject(
            method = "getPossibleEntries(ILnet/minecraft/item/ItemStack;Z)Ljava/util/List;",
            at = @At("HEAD"),
            cancellable = true)
    private static void miapi$modifyPossibleEntries(int power, ItemStack stack, boolean treasureAllowed, CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
        if (stack.getItem() instanceof ModularItem) {
            ArrayList<EnchantmentInstance> list = Lists.newArrayList();
            for (Enchantment enchantment : Registries.ENCHANTMENT) {
                if (
                        (!enchantment.isTreasure() || enchantment.isTreasure() && treasureAllowed) &&
                        enchantment.isAvailableForRandomSelection() && enchantment.canEnchant(stack)) {
                    for (int i = enchantment.getMaxLevel(); i > enchantment.getMinLevel() - 1; --i) {
                        if (power < enchantment.getMinCost(i) || power > enchantment.getMaxCost(i)) continue;
                        list.add(new EnchantmentInstance(enchantment, i));
                    }
                }
            }
            cir.setReturnValue(list);
        }
    }

    @Inject(
            method = "getLevel(Lnet/minecraft/enchantment/Enchantment;Lnet/minecraft/item/ItemStack;)I",
            at = @At("RETURN"),
            cancellable = true)
    private static void miapi$modifyPossibleEntries(Enchantment enchantment, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.getItem() instanceof ModularItem) {
            cir.setReturnValue(FakeEnchantment.getFakeLevel(enchantment, stack, cir.getReturnValue()));
        }
    }

    private static List<EnchantmentInstance> getLevels(int power, ItemStack stack, boolean treasureAllowed) {
        ArrayList<EnchantmentInstance> list = Lists.newArrayList();
        if (stack.getItem() instanceof ModularItem) {
            for (Enchantment enchantment : Registries.ENCHANTMENT) {
                if (
                        (!enchantment.isTreasure() || enchantment.isTreasure() && treasureAllowed) &&
                        enchantment.isAvailableForRandomSelection() && enchantment.canEnchant(stack)) {
                    for (int i = enchantment.getMaxLevel(); i > enchantment.getMinLevel() - 1; --i) {
                        if (power < enchantment.getMinCost(i) || power > enchantment.getMaxCost(i)) continue;
                        list.add(new EnchantmentInstance(enchantment, i));
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
    private static void miapi$addFakeEnchants(EnchantmentHelper.EnchantmentVisitor consumer, ItemStack stack, CallbackInfo ci) {
        FakeEnchantment.addEnchantments(consumer, stack);
    }
}
