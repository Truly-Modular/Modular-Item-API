package smartin.miapi.modules.abilities.toolabilities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import smartin.miapi.mixin.ShovelItemAccessor;
import smartin.miapi.modules.abilities.util.ToolAbilities;

import java.util.Optional;

public class ShovelAbility extends ToolAbilities {
    public final static String KEY = "shovel_ability";

    @Override
    public Optional<BlockState> getBlockState(BlockState blockState, UseOnContext context) {
        return Optional.empty();
    }

    public InteractionResult useOnBlock(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        BlockState blockState = world.getBlockState(blockPos);
        if (context.getClickedFace() == Direction.DOWN) {
            return InteractionResult.PASS;
        } else {
            Player playerEntity = context.getPlayer();
            BlockState blockState2 = ShovelItemAccessor.getPATH_STATES().get(blockState.getBlock());
            BlockState blockState3 = null;
            if (blockState2 != null && world.getBlockState(blockPos.above()).isAir()) {
                world.playSound(playerEntity, blockPos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
                blockState3 = blockState2;
            } else if (blockState.getBlock() instanceof CampfireBlock && blockState.getValue(CampfireBlock.LIT)) {
                if (!world.isClientSide()) {
                    world.levelEvent((Player)null, 1009, blockPos, 0);
                }

                CampfireBlock.dowse(context.getPlayer(), world, blockPos, blockState);
                blockState3 = blockState.setValue(CampfireBlock.LIT, false);
            }

            if (blockState3 != null) {
                if (!world.isClientSide) {
                    world.setBlock(blockPos, blockState3, 11);
                    world.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(playerEntity, blockState3));
                    if (playerEntity != null) {
                        context.getItemInHand().hurtAndBreak(1, playerEntity, getEquipmentSlot(context.getHand()));
                    }
                }

                return InteractionResult.sidedSuccess(world.isClientSide);
            } else {
                return InteractionResult.PASS;
            }
        }
    }
}
