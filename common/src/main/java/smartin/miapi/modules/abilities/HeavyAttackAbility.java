package smartin.miapi.modules.abilities;

import com.redpxnda.nucleus.network.clientbound.ParticleCreationPacket;
import smartin.miapi.mixin.LivingEntityAccessor;
import smartin.miapi.modules.abilities.util.*;
import smartin.miapi.modules.properties.HeavyAttackProperty;
import smartin.miapi.modules.properties.LoreProperty;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/**
 * This Ability allows a stronger attack than the normal left click.
 * Has Configurable range and default sweeping and a scale factor for Damage
 */
public class HeavyAttackAbility implements ItemUseDefaultCooldownAbility, ItemUseMinHoldAbility {

    public HeavyAttackAbility() {
        LoreProperty.bottomLoreSuppliers.add(itemStack -> {
            List<Component> texts = new ArrayList<>();
            if (AbilityMangerProperty.isPrimaryAbility(this, itemStack)) {
                texts.add(Component.translatable("miapi.ability.heavy_attack.lore"));
            }
            return texts;
        });
    }

    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        return HeavyAttackProperty.property.hasHeavyAttack(itemStack) || getAbilityContext(itemStack).getDouble("damage", 0) != 0;
    }

    @Override
    public UseAnim getUseAction(ItemStack itemStack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        if (user.getCooldowns().isOnCooldown(user.getItemInHand(hand).getItem())) {
            return InteractionResultHolder.pass(user.getItemInHand(hand));
        }
        user.startUsingItem(hand);
        return InteractionResultHolder.consume(user.getItemInHand(hand));
    }


    @Override
    public void onStoppedUsingAfter(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        HeavyAttackProperty.HeavyAttackHolder heavyAttackJson = HeavyAttackProperty.property.get(stack);
        AbilityMangerProperty.AbilityContext context = getAbilityContext(stack);
        double damage;
        double sweeping;
        double range;
        double minHold;
        double cooldown;
        if (heavyAttackJson != null) {
            damage = context.getDouble("damage", heavyAttackJson.damage);
            sweeping = context.getDouble("sweeping", heavyAttackJson.sweeping);
            range = context.getDouble("range", heavyAttackJson.range);
            minHold = context.getDouble("minHold", heavyAttackJson.minHold);
            cooldown = context.getDouble("cooldown", heavyAttackJson.cooldown);
        } else {
            damage = context.getDouble("damage", 1.0);
            sweeping = context.getDouble("sweeping", 0.0);
            range = context.getDouble("range", 3.5);
            minHold = context.getDouble("minHold", 20);
            cooldown = context.getDouble("cooldown", 20);
        }

        if (user instanceof Player player && getMaxUseTime(stack) - remainingUseTicks > minHold) {
            EntityHitResult entityHitResult = AttackUtil.raycastFromPlayer(range, player);
            if (entityHitResult != null) {
                Entity target2 = entityHitResult.getEntity();
                if (target2 instanceof LivingEntity target) {
                    ((LivingEntityAccessor) player).attacking(target);
                    damage = ((float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * damage);
                    AttackUtil.performAttack(player, target, (float) damage, true);
                    if (sweeping > 0) {
                        AttackUtil.performSweeping(player, target, (float) sweeping, (float) damage);
                    }
                    player.swing(player.getUsedItemHand());
                    player.getCooldowns().addCooldown(stack.getItem(), (int) cooldown);
                    if (player.level() instanceof ServerLevel serverWorld) {
                        if (heavyAttackJson!=null && heavyAttackJson.particleEffect != null) {
                            ParticleCreationPacket particleCreationPacket = new ParticleCreationPacket(heavyAttackJson.particleEffect, player.getX(), player.getY(), player.getZ(), 0, 0, 0);
                            particleCreationPacket.send(serverWorld);
                        }
                    }
                }
            }
        }
    }
}
