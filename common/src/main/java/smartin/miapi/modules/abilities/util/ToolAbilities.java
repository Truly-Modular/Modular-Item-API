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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ModuleInstance;

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
    public int getMaxUseTime(ItemStack itemStack) {
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

    public void initialize(ToolAbilityContext data, ModuleInstance moduleInstance) {
        data.cd = data.cooldown.evaluate(moduleInstance).intValue();
        data.minUse = data.minUseTime.evaluate(moduleInstance).intValue();
    }

    @Override
    public int getCooldown(ItemStack itemstack) {
        return getSpecialContext(itemstack).cd;
    }

    @Override
    public int getMinHoldTime(ItemStack itemStack) {
        return getSpecialContext(itemStack).minUse;
    }

    @Override
    public ToolAbilityContext getDefaultContext() {
        return null;
    }

    public static class ToolAbilityContext {
        @AutoCodec.Name("min_hold_time")
        @CodecBehavior.Optional
        public StatResolver.DoubleFromStat minUseTime = new StatResolver.DoubleFromStat(0);
        @CodecBehavior.Optional
        public StatResolver.DoubleFromStat cooldown = new StatResolver.DoubleFromStat(0);
        @AutoCodec.Ignored
        public int minUse = 0;
        @AutoCodec.Ignored
        public int cd = 0;
    }
}
