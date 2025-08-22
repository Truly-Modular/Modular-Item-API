package smartin.miapi.modules.abilities;

import com.mojang.serialization.MapCodec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
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
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.MinMaxCDAbility;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.List;

public class AreaHarvestReplant extends MinMaxCDAbility<AreaHarvestReplant.AreaHarvestJson> {
    public static String KEY = "area_harvest_ability";
    public static MapCodec<AreaHarvestJson> CODEC = AutoCodec.of(AreaHarvestJson.class);

    public AreaHarvestReplant() {
        super(0, 0);
    }

    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext, MinMaxCDData<AreaHarvestJson> context) {
        if (
                abilityHitContext.hitEntity() == null &&
                abilityHitContext.hitResult() != null) {
            BlockState state = abilityHitContext.hitResult().getLevel().getBlockState(abilityHitContext.hitResult().getClickedPos());
            return isGrown(state);
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
    public UseAnim getUseAction(ItemStack itemStack, MinMaxCDData<AreaHarvestJson> context) {
        return UseAnim.BRUSH;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand, MinMaxCDData<AreaHarvestJson> context) {
        return null;
    }

    public InteractionResult useOnBlock(UseOnContext context, MinMaxCDData<AreaHarvestJson> abilityContext) {
        ItemStack itemStack = context.getItemInHand();
        if (!context.getLevel().isClientSide() && context.getPlayer() instanceof ServerPlayer serverPlayer) {
            int blocksHarvested = 0;
            int range = getData(itemStack).map(a -> a.range.getValue()).orElse(1.0).intValue();
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
                            List<ItemStack> stacks = Block.getDrops(blockState, serverWorld, currentPos, blockEntity, serverPlayer, itemStack);
                            serverWorld.setBlockAndUpdate(currentPos, cropBlock.getStateForAge(0));
                            stacks.forEach(serverPlayer::spawnAtLocation);
                            blocksHarvested++;
                        }
                    }
                }
            }

            itemStack.hurtAndBreak(blocksHarvested, serverPlayer, getEquipmentSlot(context.getHand()));

            return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
        }
        return InteractionResult.FAIL;
    }

    @Override
    protected AreaHarvestJson mergeData(AreaHarvestJson left, AreaHarvestJson right, MergeType mergeType) {
        return left;
    }

    public AreaHarvestJson initializeData(AreaHarvestJson data, ModuleInstance moduleInstance) {
        return data.initialize(moduleInstance);
    }

    @Override
    protected MapCodec<AreaHarvestJson> getMapCodec() {
        return CODEC;
    }

    public static class AreaHarvestJson {
        @CodecBehavior.Optional
        public DoubleOperationResolvable range = new DoubleOperationResolvable(0);

        public AreaHarvestJson initialize(ModuleInstance moduleInstance) {
            AreaHarvestJson init = new AreaHarvestJson();
            init.range = range.initialize(moduleInstance);
            return init;
        }

    }
}
