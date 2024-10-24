package smartin.miapi.modules.abilities;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseDefaultCooldownAbility;
import smartin.miapi.modules.abilities.util.ItemUseMinHoldAbility;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.List;

public class SonicBoomAbility implements ItemUseDefaultCooldownAbility<SonicBoomAbility.SonicBoomContext>, ItemUseMinHoldAbility<SonicBoomAbility.SonicBoomContext> {
    public static String KEY = "sonic_boom";

    @Override
    public int getCooldown(ItemStack itemStack) {
        return (int) getSpecialContext(itemStack).cooldown().getValue();
    }

    @Override
    public int getMinHoldTime(ItemStack itemStack) {
        return (int) getSpecialContext(itemStack).minHold().getValue();
    }

    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        return true;
    }

    @Override
    public UseAnim getUseAction(ItemStack itemStack) {
        return UseAnim.SPYGLASS;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack, LivingEntity entity) {
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
        SonicBoomContext context = getSpecialContext(stack);
        if (user instanceof Player player && getMaxUseTime(stack, user) - remainingUseTicks >= context.minHold().getValue()) {
            if (world instanceof ServerLevel serverLevel) {
                double range = context.maxRange().getValue();
                AABB aabb = new AABB(player.getX() - range, player.getY() - range, player.getZ() - range,
                        player.getX() + range, player.getY() + range, player.getZ() + range);
                List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, aabb,
                        entity -> entity != player && entity.isAlive());

                for (LivingEntity target : entities) {
                    double damage = context.damage().getValue();
                    target.hurt(player.damageSources().sonicBoom(target), (float) damage);

                    // Visual effect
                    serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getY(), target.getZ(),
                            10, 0.5, 0.5, 0.5, 0.1);
                }

                // Play sound
                player.playSound(context.onBoom(), 1.0f, 1.0f);

                // Apply cooldown
                player.getCooldowns().addCooldown(stack.getItem(), (int) context.cooldown().getValue());
                player.swing(player.getUsedItemHand());
            }
        }
    }

    @Override
    public <K> SonicBoomContext decode(DynamicOps<K> ops, K prefix) {
        return SonicBoomContext.CODEC.decode(ops, prefix).getOrThrow().getFirst();
    }

    @Override
    public SonicBoomContext getDefaultContext() {
        return new SonicBoomContext(
                SoundEvents.WARDEN_SONIC_BOOM,
                new DoubleOperationResolvable(15),
                new DoubleOperationResolvable(10),
                new DoubleOperationResolvable(20),
                new DoubleOperationResolvable(5));
    }

    @Override
    public SonicBoomContext initialize(SonicBoomContext json, ModuleInstance moduleInstance) {
        return json.initialize(moduleInstance);
    }

    @Override
    public SonicBoomContext merge(SonicBoomContext left, SonicBoomContext right, MergeType mergeType) {
        return SonicBoomContext.merge(left, right, mergeType);
    }

    public record SonicBoomContext(SoundEvent onBoom, DoubleOperationResolvable minHold,
                                   DoubleOperationResolvable damage, DoubleOperationResolvable cooldown,
                                   DoubleOperationResolvable maxRange) {
        public static final Codec<SonicBoomContext> CODEC = RecordCodecBuilder.create((instance) ->
                instance.group(
                        SoundEvent.DIRECT_CODEC.optionalFieldOf("on_boom", SoundEvents.WARDEN_SONIC_BOOM)
                                .forGetter(SonicBoomContext::onBoom),
                        DoubleOperationResolvable.CODEC.optionalFieldOf("min_hold", new DoubleOperationResolvable(15))
                                .forGetter(SonicBoomContext::minHold),
                        DoubleOperationResolvable.CODEC.optionalFieldOf("damage", new DoubleOperationResolvable(10))
                                .forGetter(SonicBoomContext::damage),
                        DoubleOperationResolvable.CODEC.optionalFieldOf("cooldown", new DoubleOperationResolvable(20))
                                .forGetter(SonicBoomContext::cooldown),
                        DoubleOperationResolvable.CODEC.optionalFieldOf("max_range", new DoubleOperationResolvable(5))
                                .forGetter(SonicBoomContext::maxRange)
                ).apply(instance, SonicBoomContext::new)
        );

        public SonicBoomContext initialize(ModuleInstance moduleInstance) {
            DoubleOperationResolvable initializedMinHold = minHold.initialize(moduleInstance);
            DoubleOperationResolvable initializedDamage = damage.initialize(moduleInstance);
            DoubleOperationResolvable initializedCooldown = cooldown.initialize(moduleInstance);
            DoubleOperationResolvable initializedMaxRange = maxRange.initialize(moduleInstance);

            return new SonicBoomContext(onBoom, initializedMinHold, initializedDamage, initializedCooldown, initializedMaxRange);
        }

        public static SonicBoomContext merge(SonicBoomContext left, SonicBoomContext right, MergeType mergeType) {
            SoundEvent mergedOnBoom = MergeType.EXTEND.equals(mergeType) && left.onBoom != null ? left.onBoom : right.onBoom;
            DoubleOperationResolvable mergedMinHold = left.minHold.merge(right.minHold, mergeType);
            DoubleOperationResolvable mergedDamage = left.damage.merge(right.damage, mergeType);
            DoubleOperationResolvable mergedCooldown = left.cooldown.merge(right.cooldown, mergeType);
            DoubleOperationResolvable mergedMaxRange = left.maxRange.merge(right.maxRange, mergeType);

            return new SonicBoomContext(mergedOnBoom, mergedMinHold, mergedDamage, mergedCooldown, mergedMaxRange);
        }
    }
}

