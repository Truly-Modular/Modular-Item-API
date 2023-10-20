package smartin.miapi.modules.abilities.toolabilities;

import com.google.common.collect.BiMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.PillarBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import smartin.miapi.mixin.AxeItemAccessor;
import smartin.miapi.modules.abilities.ToolAbilities;

import java.util.Optional;

public class AxeAbility extends ToolAbilities {

    public final static String KEY = "axe_ability";

    @Override
    public Optional<BlockState> getBlockState(BlockState blockState, ItemUsageContext context) {
        Optional<BlockState> optional = this.getStrippedState(blockState);
        Optional<BlockState> optional2 = Oxidizable.getDecreasedOxidationState(blockState);
        Optional<BlockState> optional3 = Optional.ofNullable((Block) ((BiMap) HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get()).get(blockState.getBlock())).map((block) -> {
            return block.getStateWithProperties(blockState);
        });
        Optional<BlockState> optional4 = Optional.empty();
        World world = context.getWorld();
        PlayerEntity playerEntity = context.getPlayer();
        BlockPos blockPos = context.getBlockPos();
        if (optional.isPresent()) {
            world.playSound(playerEntity, blockPos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
            optional4 = optional;
        } else if (optional2.isPresent()) {
            world.playSound(playerEntity, blockPos, SoundEvents.ITEM_AXE_SCRAPE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.syncWorldEvent(playerEntity, 3005, blockPos, 0);
            optional4 = optional2;
        } else if (optional3.isPresent()) {
            world.playSound(playerEntity, blockPos, SoundEvents.ITEM_AXE_WAX_OFF, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.syncWorldEvent(playerEntity, 3004, blockPos, 0);
            optional4 = optional3;
        }
        return optional4;
    }

    private Optional<BlockState> getStrippedState(BlockState state) {
        return Optional.ofNullable(AxeItemAccessor.getSTRIPPED_BLOCKS().get(state.getBlock())).map((block) -> {
            return (BlockState) block.getDefaultState().with(PillarBlock.AXIS, state.get(PillarBlock.AXIS));
        });
    }
}
