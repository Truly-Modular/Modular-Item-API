package smartin.miapi.modules.abilities.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.Optional;

public abstract class ToolAbilities implements ItemUseDefaultCooldownAbility<ToolAbilities.ToolAbilityContext>, ItemUseMinHoldAbility<ToolAbilities.ToolAbilityContext> {
    public static Codec<ToolAbilities.ToolAbilityContext> CODEC = AutoCodec.of(ToolAbilityContext.class).codec();

    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        return abilityHitContext.hitResult() != null;
    }

    @Override
    public UseAnim getUseAction(ItemStack itemStack) {
        return null;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack, LivingEntity entity) {
        return 0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        return null;
    }

    /**
     * Also play sounds if needed in this call
     *
     * @param blockState
     * @return
     */
    public abstract Optional<BlockState> getBlockState(BlockState blockState, UseOnContext context);

    @Override
    public InteractionResult useOnBlock(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        Player playerEntity = context.getPlayer();
        BlockState blockState = world.getBlockState(blockPos);
        Optional<BlockState> changedBlock = getBlockState(blockState, context);

        if (changedBlock.isPresent()) {
            if (playerEntity instanceof ServerPlayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) playerEntity, blockPos, context.getItemInHand());
            }

            world.setBlock(blockPos, changedBlock.get(), 11);
            world.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(playerEntity, changedBlock.get()));
            if (playerEntity != null) {
                EquipmentSlot equipmentSlot = context.getHand().equals(InteractionHand.MAIN_HAND) ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
                context.getItemInHand().hurtAndBreak(1, playerEntity, equipmentSlot);
            }

            return InteractionResult.sidedSuccess(world.isClientSide);
        } else {
            return InteractionResult.PASS;
        }
    }

    public <K> ToolAbilityContext decode(DynamicOps<K> ops, K prefix) {
        return CODEC.decode(ops, prefix).getOrThrow().getFirst();
    }

    public ToolAbilityContext initialize(ToolAbilityContext data, ModuleInstance moduleInstance) {
        ToolAbilityContext context = new ToolAbilityContext();
        context.cooldown = data.cooldown.initialize(moduleInstance);
        context.minUseTime = data.minUseTime.initialize(moduleInstance);
        return context;
    }

    @Override
    public int getCooldown(ItemStack itemstack) {
        return (int) getSpecialContext(itemstack).cooldown.getValue();
    }

    @Override
    public int getMinHoldTime(ItemStack itemStack) {
        return (int) getSpecialContext(itemStack).minUseTime.getValue();
    }

    @Override
    public ToolAbilityContext getDefaultContext() {
        return null;
    }

    @Override
    public ToolAbilityContext merge(ToolAbilityContext right, ToolAbilityContext left, MergeType mergeType) {
        ToolAbilityContext context = new ToolAbilityContext();
        context.minUseTime = left.minUseTime.merge(right.minUseTime, mergeType);
        context.cooldown = left.cooldown.merge(right.cooldown, mergeType);
        return context;
    }

    public static class ToolAbilityContext {
        @AutoCodec.Name("min_hold_time")
        @CodecBehavior.Optional
        public DoubleOperationResolvable minUseTime = new DoubleOperationResolvable( 0);
        @CodecBehavior.Optional
        public DoubleOperationResolvable cooldown = new DoubleOperationResolvable( 0);
    }
}
