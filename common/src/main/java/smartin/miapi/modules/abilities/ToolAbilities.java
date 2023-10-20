package smartin.miapi.modules.abilities;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseAbility;

import java.util.Optional;

public abstract class ToolAbilities implements ItemUseAbility {
    @Override
    public boolean allowedOnItem(ItemStack itemStack, World world, PlayerEntity player, Hand hand, ItemAbilityManager.AbilityContext abilityContext) {
        AxeItem toolItem;
        return abilityContext.hitResult() != null;
    }

    @Override
    public UseAction getUseAction(ItemStack itemStack) {
        return null;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack) {
        return 0;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return null;
    }

    /**
     * Also play sounds if needed in this call
     *
     * @param blockState
     * @return
     */
    public abstract Optional<BlockState> getBlockState(BlockState blockState, ItemUsageContext context);

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        PlayerEntity playerEntity = context.getPlayer();
        BlockState blockState = world.getBlockState(blockPos);
        Optional<BlockState> changedBlock = getBlockState(blockState, context);

        if (changedBlock.isPresent()) {
            if (playerEntity instanceof ServerPlayerEntity) {
                Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity) playerEntity, blockPos, context.getStack());
            }

            world.setBlockState(blockPos, changedBlock.get(), 11);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Emitter.of(playerEntity, changedBlock.get()));
            if (playerEntity != null) {
                context.getStack().damage(1, playerEntity, (p) -> {
                    p.sendToolBreakStatus(context.getHand());
                });
            }

            return ActionResult.success(world.isClient);
        } else {
            return ActionResult.PASS;
        }
    }
}
