package smartin.miapi.modules.properties.mining.shape;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import smartin.miapi.modules.properties.util.InitializeAble;

import java.util.List;

/**
 * Mining Shape is supposed to be the original scanner for the blocks.
 * Its the Implementation of the Algorythm for scanning blocks
 */
public interface MiningShape extends InitializeAble<MiningShape> {
    List<BlockPos> getMiningBlocks(Level world, BlockPos pos, Direction face);

    ResourceLocation getID();
}
