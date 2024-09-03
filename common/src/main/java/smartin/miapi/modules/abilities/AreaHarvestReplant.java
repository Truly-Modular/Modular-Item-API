package smartin.miapi.modules.abilities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseDefaultCooldownAbility;
import smartin.miapi.modules.abilities.util.ItemUseMinHoldAbility;
import smartin.miapi.modules.properties.AbilityMangerProperty;

import java.util.List;

public class AreaHarvestReplant implements ItemUseDefaultCooldownAbility, ItemUseMinHoldAbility {
    public static String KEY = "area_harvest_ability";

    @Override
    public boolean allowedOnItem(ItemStack itemStack, World world, PlayerEntity player, Hand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        if (
                abilityHitContext.hitEntity() == null &&
                abilityHitContext.hitResult() != null) {
            BlockState state = abilityHitContext.hitResult().getWorld().getBlockState(abilityHitContext.hitResult().getBlockPos());
            return isGrown(state);
        }
        return false;
    }

    public boolean isGrown(BlockState state) {
        if (state.getBlock() instanceof CropBlock cropBlock) {
            return cropBlock.isMature(state);

        }
        return false;
    }

    @Override
    public UseAction getUseAction(ItemStack itemStack) {
        return UseAction.BRUSH;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack) {
        return 10;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return null;
    }

    public ActionResult useOnBlock(ItemUsageContext context) {
        ItemStack itemStack = context.getStack();
        if (!context.getWorld().isClient() && context.getPlayer() instanceof ServerPlayerEntity serverPlayer) {
            int blocksHarvested = 0;
            AbilityMangerProperty.AbilityContext abilityContext = getAbilityContext(itemStack);
            int range = abilityContext.getInt("block_range", 1);
            BlockState state = context.getWorld().getBlockState(context.getBlockPos());
            BlockPos origin = context.getBlockPos();

            if (isGrown(state)) {
                for (int x = -range; x <= range; x++) {
                    for (int y = -range; y <= range; y++) {
                        BlockPos currentPos = origin.add(x, 0, y);
                        BlockState blockState = context.getWorld().getBlockState(currentPos);
                        if (isGrown(blockState) && blockState.getBlock() instanceof CropBlock cropBlock && context.getWorld() instanceof ServerWorld serverWorld) {
                            BlockEntity blockEntity = blockState.hasBlockEntity() ? context.getWorld().getBlockEntity(currentPos) : null;
                            List<ItemStack> stacks = Block.getDroppedStacks(blockState, serverWorld, currentPos, blockEntity, serverPlayer, itemStack);
                            serverWorld.setBlockState(currentPos, cropBlock.withAge(0));
                            stacks.forEach(stack -> serverPlayer.getInventory().offerOrDrop(stack));
                            blocksHarvested++;
                        }
                    }
                }
            }

            itemStack.damage(blocksHarvested, context.getWorld().getRandom(), serverPlayer);

            return ActionResult.success(context.getWorld().isClient());
        }
        return ActionResult.FAIL;
    }
}
