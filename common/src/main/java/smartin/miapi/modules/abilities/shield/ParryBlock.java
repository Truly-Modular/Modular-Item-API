package smartin.miapi.modules.abilities.shield;

import com.mojang.serialization.MapCodec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.pose.server.ServerPoseFacet;
import dev.architectury.event.EventResult;
import net.minecraft.core.Holder;
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
            if (data == null || moduleInstance == null) return EventResult.pass();

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

            return EventResult.interruptDefault();
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
                        ServerPoseFacet facet = ServerPoseFacet.KEY.get(serverPlayer);
                        if (facet != null && data.pose != null) {
                            facet.set(data.pose.toString(), serverPlayer, hand);
                        }
                    }
                }
            }
        }

        user.startUsingItem(hand);
        return InteractionResultHolder.consume(user.getItemInHand(hand));
    }


}
