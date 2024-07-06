package smartin.miapi.modules.abilities.toolabilities;

import com.google.common.collect.BiMap;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import smartin.miapi.mixin.AxeItemAccessor;
import smartin.miapi.modules.abilities.util.ToolAbilities;

import java.util.Optional;

public class AxeAbility extends ToolAbilities {

    public final static String KEY = "axe_ability";

    @Override
    public Optional<BlockState> getBlockState(BlockState blockState, UseOnContext context) {
        Optional<BlockState> optional = this.getStrippedState(blockState);
        Optional<BlockState> optional2 = WeatheringCopper.getPrevious(blockState);
        Optional<BlockState> optional3 = Optional.ofNullable((Block) ((BiMap) HoneycombItem.WAX_OFF_BY_BLOCK.get()).get(blockState.getBlock())).map((block) -> {
            return block.withPropertiesOf(blockState);
        });
        Optional<BlockState> optional4 = Optional.empty();
        Level world = context.getLevel();
        Player playerEntity = context.getPlayer();
        BlockPos blockPos = context.getClickedPos();
        if (optional.isPresent()) {
            world.playSound(playerEntity, blockPos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
            optional4 = optional;
        } else if (optional2.isPresent()) {
            world.playSound(playerEntity, blockPos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
            world.levelEvent(playerEntity, 3005, blockPos, 0);
            optional4 = optional2;
        } else if (optional3.isPresent()) {
            world.playSound(playerEntity, blockPos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
            world.levelEvent(playerEntity, 3004, blockPos, 0);
            optional4 = optional3;
        }
        return optional4;
    }

    private Optional<BlockState> getStrippedState(BlockState state) {
        return Optional.ofNullable(AxeItemAccessor.getSTRIPPABLES().get(state.getBlock())).map((block) -> {
            return (BlockState) block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
        });
    }
}
