package smartin.miapi.modules.abilities;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import com.redpxnda.nucleus.network.clientbound.ParticleCreationPacket;
import net.minecraft.core.particles.ParticleOptions;
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
import smartin.miapi.mixin.LivingEntityAccessor;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.abilities.util.*;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.List;

/**
 * This Ability allows a stronger attack than the normal left click.
 * Has Configurable range and default sweeping and a scale factor for Damage
 */
public class SpecialAttackAbility implements
        ItemUseDefaultCooldownAbility<SpecialAttackAbility.SpecialAttackJson>,
        ItemUseMinHoldAbility<SpecialAttackAbility.SpecialAttackJson> {
    public static Codec<SpecialAttackJson> CODEC = AutoCodec.of(SpecialAttackJson.class).codec();

    public SpecialAttackAbility() {
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
        return true;
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
        SpecialAttackJson specialAttackJson = getSpecialContext(stack);
        if (user instanceof Player player && getMaxUseTime(stack) - remainingUseTicks > specialAttackJson.minHold.getValue()) {
            EntityHitResult entityHitResult = AttackUtil.raycastFromPlayer(specialAttackJson.range.getValue(), player);
            if (entityHitResult != null) {
                Entity target2 = entityHitResult.getEntity();
                if (target2 instanceof LivingEntity target) {
                    ((LivingEntityAccessor) player).attacking(target);
                    float damage = (float) ((float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * specialAttackJson.damage.getValue());
                    float sweeping = (float) specialAttackJson.sweeping.getValue();
                    AttackUtil.performAttack(player, target, damage, true, stack);
                    if (sweeping > 0) {
                        AttackUtil.performSweeping(player, target, sweeping, damage);
                    }
                    player.swing(player.getUsedItemHand());
                    player.getCooldowns().addCooldown(stack.getItem(), (int) specialAttackJson.cooldown.getValue());
                    if (player.level() instanceof ServerLevel serverWorld) {
                        specialAttackJson.particleEffect.forEach(particleOptions -> {
                            ParticleCreationPacket particleCreationPacket = new ParticleCreationPacket(particleOptions, player.getX(), player.getY(), player.getZ(), 0, 0, 0);
                            particleCreationPacket.send(serverWorld);
                        });
                    }
                }
            }
        }
    }

    @Override
    public int getCooldown(ItemStack itemStack) {
        return (int) getSpecialContext(itemStack).cooldown.getValue();
    }

    @Override
    public <K> SpecialAttackJson decode(DynamicOps<K> ops, K prefix) {
        return CODEC.decode(ops, prefix).getOrThrow().getFirst();
    }

    @Override
    public SpecialAttackJson getDefaultContext() {
        return null;
    }

    @Override
    public void initialize(SpecialAttackJson json, ModuleInstance moduleInstance) {
        json.initialize(moduleInstance);
    }


    @Override
    public int getMinHoldTime(ItemStack itemStack) {
        return (int) getSpecialContext(itemStack).minHold.getValue();
    }

    Override

    public SpecialAttackJson merge(SpecialAttackJson left, SpecialAttackJson right, MergeType mergeType) {
        SpecialAttackJson merged = new SpecialAttackJson();
        merged.damage = left.damage.merge(right.damage, mergeType);
        merged.sweeping = left.sweeping.merge(right.sweeping, mergeType);
        merged.range = left.range.merge(right.range, mergeType);
        merged.minHold = left.minHold.merge(right.minHold, mergeType);
        merged.cooldown = left.cooldown.merge(right.cooldown, mergeType);
        if (MergeType.EXTEND.equals(mergeType)) {
            merged.description = left.description;
            merged.title = left.description;
        } else {
            merged.description = right.description;
            merged.title = right.description;
        }
        if (MergeType.OVERWRITE.equals(mergeType)) {
            merged.particleEffect = right.particleEffect;
        } else {
            List<ParticleOptions> list = new ArrayList<>(right.particleEffect);
            list.addAll(left.particleEffect);
            merged.particleEffect = list;
        }
        return merged;
    }

    public static class SpecialAttackJson {
        public DoubleOperationResolvable damage = new DoubleOperationResolvable(0, 1);
        public DoubleOperationResolvable sweeping = new DoubleOperationResolvable(0, 0);
        public DoubleOperationResolvable range = new DoubleOperationResolvable(3.5, 3.5);
        @AutoCodec.Name("min_hold")
        public DoubleOperationResolvable minHold = new DoubleOperationResolvable(0, 0);
        public DoubleOperationResolvable cooldown = new DoubleOperationResolvable(0, 0);
        @CodecBehavior.Optional
        public Component title = Component.translatable("miapi.ability.heavy_attack.title");
        @CodecBehavior.Optional
        public Component description = Component.translatable("miapi.ability.heavy_attack.description");
        @CodecBehavior.Optional
        public List<ParticleOptions> particleEffect = new ArrayList<>();

        public void initialize(ModuleInstance moduleInstance) {
            damage.initialize(moduleInstance);
            sweeping.initialize(moduleInstance);
            range.initialize(moduleInstance);
            minHold.initialize(moduleInstance);
            cooldown.initialize(moduleInstance);
        }
    }
}
