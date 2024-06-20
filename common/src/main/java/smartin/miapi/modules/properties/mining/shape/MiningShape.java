package smartin.miapi.modules.properties.mining.shape;

import com.google.gson.JsonObject;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import smartin.miapi.modules.ItemModule;

import java.util.List;

/**
 * Mining Shape is supposed to be the original scanner for the blocks.
 * Its the Implementation of the Algorythm for scanning blocks
 */
public interface MiningShape {
    MiningShape fromJson(JsonObject object, ItemModule.ModuleInstance moduleInstance);

    List<BlockPos> getMiningBlocks(World world, BlockPos pos, Direction face);
}
