package smartin.miapi.modules.abilities;

import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseAbility;
import smartin.miapi.modules.abilities.util.ItemUseDefaultCooldownAbility;
import smartin.miapi.modules.abilities.util.ItemUseMinHoldAbility;

import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public abstract class ToolAbilities implements ItemUseDefaultCooldownAbility, ItemUseMinHoldAbility {
    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        AxeItem toolItem;
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
                context.getItemInHand().hurtAndBreak(1, playerEntity, (p) -> {
                    p.sendToolBreakStatus(context.getHand());
                });
            }

            return InteractionResult.sidedSuccess(world.isClientSide);
        } else {
            return InteractionResult.PASS;
        }
    }
}
