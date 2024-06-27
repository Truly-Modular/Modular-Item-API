package smartin.miapi.modules.abilities;

import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseDefaultCooldownAbility;
import smartin.miapi.modules.abilities.util.ItemUseMinHoldAbility;
import smartin.miapi.modules.properties.AbilityMangerProperty;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AreaHarvestReplant implements ItemUseDefaultCooldownAbility, ItemUseMinHoldAbility {
    public static String KEY = "area_harvest_ability";

    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        if (
                abilityHitContext.hitEntity() == null &&
                abilityHitContext.hitResult() != null) {
            BlockState state = abilityHitContext.hitResult().getLevel().getBlockState(abilityHitContext.hitResult().getClickedPos());
            if (isGrown(state)) {
                return true;
            }
        }
        return false;
    }

    public boolean isGrown(BlockState state) {
        if (state.getBlock() instanceof CropBlock cropBlock) {
            return cropBlock.isMaxAge(state);

        }
        return false;
    }

    @Override
    public UseAnim getUseAction(ItemStack itemStack) {
        return UseAnim.BRUSH;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack) {
        return 10;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        return null;
    }

    public InteractionResult useOnBlock(UseOnContext context) {
        ItemStack itemStack = context.getItemInHand();
        if (!context.getLevel().isClientSide() && context.getPlayer() instanceof ServerPlayer serverPlayer) {
            int blocksHarvested = 0;
            AbilityMangerProperty.AbilityContext abilityContext = getAbilityContext(itemStack);
            int range = abilityContext.getInt("block_range", 1);
            BlockState state = context.getLevel().getBlockState(context.getClickedPos());
            BlockPos origin = context.getClickedPos();

            if (isGrown(state)) {
                for (int x = -range; x <= range; x++) {
                    for (int y = -range; y <= range; y++) {
                        BlockPos currentPos = origin.offset(x, 0, y);
                        BlockState blockState = context.getLevel().getBlockState(currentPos);
                        if (isGrown(blockState) && blockState.getBlock() instanceof CropBlock cropBlock && context.getLevel() instanceof ServerLevel serverWorld) {
                            //cropBlock.

                            BlockEntity blockEntity = blockState.hasBlockEntity() ? context.getLevel().getBlockEntity(currentPos) : null;
                            List<ItemStack> stacks = Block.getDrops(blockState, serverWorld, currentPos, blockEntity, serverPlayer, ItemStack.EMPTY);
                            serverWorld.setBlockAndUpdate(currentPos, cropBlock.getStateForAge(0));
                            stacks.forEach(serverPlayer::spawnAtLocation);
                            blocksHarvested++;
                        }
                    }
                }
            }

            itemStack.hurtAndBreak(blocksHarvested, context.getLevel().getRandom(), serverPlayer);

            return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
        }
        return InteractionResult.FAIL;
    }
}
