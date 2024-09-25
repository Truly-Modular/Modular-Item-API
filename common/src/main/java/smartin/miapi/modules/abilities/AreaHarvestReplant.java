package smartin.miapi.modules.abilities;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseDefaultCooldownAbility;
import smartin.miapi.modules.abilities.util.ItemUseMinHoldAbility;

import java.util.List;

public class AreaHarvestReplant implements ItemUseDefaultCooldownAbility<AreaHarvestReplant.AreaHarvestJson>, ItemUseMinHoldAbility<AreaHarvestReplant.AreaHarvestJson> {
    public static String KEY = "area_harvest_ability";
    public static Codec<AreaHarvestJson> CODEC = AutoCodec.of(AreaHarvestJson.class).codec();

    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
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
    public UseAnim getUseAction(ItemStack itemStack) {
        return UseAnim.BRUSH;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack, LivingEntity entity) {
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
            int range = getSpecialContext(itemStack).range.evaluatedOutput;
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

            itemStack.hurtAndBreak(blocksHarvested, serverPlayer, getEquipmentSlot(context.getHand()));

            return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
        }
        return InteractionResult.FAIL;
    }

    public <K> AreaHarvestJson decode(DynamicOps<K> ops, K prefix) {
        return CODEC.decode(ops, prefix).getOrThrow().getFirst();
    }

    public AreaHarvestJson initialize(AreaHarvestJson data, ModuleInstance moduleInstance) {
        return data.initialize(moduleInstance);
    }

    @Override
    public AreaHarvestJson getDefaultContext() {
        return null;
    }

    @Override
    public int getCooldown(ItemStack itemstack) {
        return getSpecialContext(itemstack).cooldown.evaluatedOutput;
    }

    @Override
    public int getMinHoldTime(ItemStack itemStack) {
        return getSpecialContext(itemStack).minUseTime.evaluatedOutput;
    }

    public static class AreaHarvestJson {
        //TODO:move to DoubleResovlables?
        @CodecBehavior.Optional
        @AutoCodec.Name("min_hold_time")
        public StatResolver.IntegerFromStat minUseTime = new StatResolver.IntegerFromStat(0);
        @CodecBehavior.Optional
        public StatResolver.IntegerFromStat cooldown = new StatResolver.IntegerFromStat(0);
        @CodecBehavior.Optional
        public StatResolver.IntegerFromStat range = new StatResolver.IntegerFromStat(1);

        public AreaHarvestJson initialize(ModuleInstance moduleInstance) {
            AreaHarvestJson init = new AreaHarvestJson();
            init.cooldown = cooldown;
            init.minUseTime = minUseTime;
            init.range = range;
            init.cooldown.evaluate(moduleInstance);
            init.minUseTime.evaluate(moduleInstance);
            init.range.evaluate(moduleInstance);
            return init;
        }

    }
}
