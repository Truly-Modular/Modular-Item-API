package smartin.miapi.modules.abilities.util;

import com.mojang.serialization.MapCodec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
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
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.Optional;

public abstract class ToolAbilities extends MinMaxCDAbility<ToolAbilities.ToolAbilityContext> {
    public static MapCodec<ToolAbilities.ToolAbilityContext> CODEC = AutoCodec.of(ToolAbilityContext.class);

    public ToolAbilities() {
        super(0, 0);
    }

    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext, MinMaxCDData<ToolAbilityContext> context) {
        return abilityHitContext.hitResult() != null;
    }

    @Override
    public UseAnim getUseAction(ItemStack itemStack, MinMaxCDData<ToolAbilityContext> context) {
        return UseAnim.NONE;
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand, MinMaxCDData<ToolAbilityContext> context) {
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
    public InteractionResult useOnBlock(UseOnContext context, MinMaxCDData<ToolAbilityContext> abilityContext) {
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

    public ToolAbilityContext mergeData(ToolAbilityContext left, ToolAbilityContext right, MergeType mergeType) {
        return MergeAble.decideLeftRight(left, right, mergeType);
    }

    public ToolAbilityContext initializeData(ToolAbilityContext property, ModuleInstance moduleInstance) {
        return property;
    }

    public MapCodec<ToolAbilityContext> getMapCodec() {
        return CODEC;
    }

    public static class ToolAbilityContext {
    }
}
