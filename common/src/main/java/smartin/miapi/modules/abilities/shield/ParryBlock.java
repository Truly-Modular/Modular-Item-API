package smartin.miapi.modules.abilities.shield;

import com.mojang.serialization.MapCodec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.pose.server.ServerPoseFacet;
import dev.architectury.event.EventResult;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.MinMaxCDAbility;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.registries.RegistryInventory;

public class ParryBlock extends MinMaxCDAbility<BlockData> {
    public static final String KEY = "parry_block";
    public static final MapCodec<BlockData> CODEC = AutoCodec.of(BlockData.class);

    public ParryBlock() {
        super(0, 20, 30);

        MiapiEvents.LIVING_HURT.register(event -> {
            if (!(event.defender instanceof Player player)) return EventResult.pass();
            ItemStack stack = player.getUseItem();
            if (stack == null || stack.isEmpty()) return EventResult.pass();

            ModuleInstance moduleInstance = ItemModule.getModules(stack);
            BlockData data = getData(stack).orElse(null);
            if (data == null || moduleInstance == null || event.attacker == null) return EventResult.pass();

            double allowedAngle = data.angle.getValue();

            // Player look direction
            Vec3 playerLook = player.getLookAngle().normalize();

            // Direction from player to attacker
            Vec3 toAttacker = event.attacker.position().subtract(player.position()).normalize();

            // Calculate angle between the vectors (in degrees)
            double angle = Math.toDegrees(Math.acos(playerLook.dot(toAttacker)));

            if (angle <= allowedAngle) {
                // Direction from player to attacker
                toAttacker = event.attacker.getEyePosition().subtract(player.position()).normalize();

                // Calculate angle between the vectors (in degrees)
                angle = Math.toDegrees(Math.acos(playerLook.dot(toAttacker)));
                if (angle <= allowedAngle) {
                    return EventResult.interruptTrue();
                }
            }


            player.getCooldowns().addCooldown(stack.getItem(), getCooldown(stack));

            if (event.damageSource.getEntity() instanceof LivingEntity attacker) {
                int attackerCD = (int) data.cooldownAttackerWeapon.getValue();
                if (attacker instanceof Player p) {
                    ItemStack attackStack = p.getMainHandItem();
                    if (!attackStack.isEmpty()) {
                        p.getCooldowns().addCooldown(attackStack.getItem(), attackerCD);
                    }
                } else {
                    // Apply a stun effect

                    attacker.addEffect(new MobEffectInstance(RegistryInventory.stunEffect, attackerCD));
                }

                float returnPercent = (float) data.damageReturnPercent.getValue() / 100f;
                float reflected = event.amount * returnPercent;

                attacker.hurt(player.damageSources().playerAttack(player), reflected);
            }

            // Play sound
            if (data.sound != null) {
                Holder<SoundEvent> holder = Holder.direct(SoundEvent.createVariableRangeEvent(data.sound));
                player.playSound(holder.value(), (float) data.volume.getValue(), (float) data.pitch.getValue());
            }
            double blocking = data.blocking.getValue();
            if (blocking >= 100) {
                return EventResult.interruptDefault();
            }
            if (blocking > 0) {
                float blockPercent = (float) blocking / 100f;
                event.amount = event.amount / blockPercent;
                return EventResult.pass();
            }
            return EventResult.pass();
        });
    }


    @Override
    protected MapCodec<BlockData> getMapCodec() {
        return BlockData.CODEC;
    }

    @Override
    protected BlockData mergeData(BlockData left, BlockData right, MergeType mergeType) {
        return left.merge(left, right, mergeType);
    }

    @Override
    public BlockData initializeData(BlockData data, ModuleInstance moduleInstance) {
        return data.initialize(data, moduleInstance);
    }

    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        return true;
    }

    @Override
    public UseAnim getUseAction(ItemStack itemStack) {
        return UseAnim.NONE;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        if (!world.isClientSide) {
            if (user instanceof ServerPlayer serverPlayer) {
                ModuleInstance moduleInstance = ItemModule.getModules(user.getItemInHand(hand));
                if (moduleInstance != null) {
                    BlockData data = getData(user.getItemInHand(hand)).orElse(null);
                    if (data != null) {
                        // Pose animation setup
                        if (data.pose != null) {
                            setAnimation(serverPlayer, data.pose, hand);
                        }
                    }
                }
            }
        }

        user.startUsingItem(hand);
        return InteractionResultHolder.consume(user.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, Level world, LivingEntity user) {
        resetAnimation(user);
        applyCooldownMissTime(stack, user);
        return super.finishUsing(stack, world, user);
    }

    @Override
    public void onStoppedUsingAfter(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        resetAnimation(user);
        super.onStoppedUsingAfter(stack, world, user, remainingUseTicks);
        applyCooldownMissTime(stack, user);
    }

    @Override
    public void onStoppedHolding(ItemStack stack, Level world, LivingEntity user) {
        resetAnimation(user);
        super.onStoppedHolding(stack, world, user);
        applyCooldownMissTime(stack, user);
    }

    public void applyCooldownMissTime(ItemStack itemStack, LivingEntity livingEntity) {
        BlockData data = getData(itemStack).orElse(null);
        if (livingEntity instanceof ServerPlayer serverPlayer && data != null) {
            serverPlayer.getCooldowns().addCooldown(itemStack.getItem(), (int) data.cooldownMissTime.getValue());
        }
    }

    public void setAnimation(Player p, ResourceLocation id, InteractionHand hand) {
        if (p instanceof ServerPlayer player) {
            ServerPoseFacet facet = ServerPoseFacet.KEY.get(player);
            if (facet != null) {
                facet.set(id.toString(), player, hand);
            }
        }
    }

    public void resetAnimation(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            ServerPoseFacet facet = ServerPoseFacet.KEY.get(player);
            if (facet != null)
                facet.reset(player);
        }
    }
}
