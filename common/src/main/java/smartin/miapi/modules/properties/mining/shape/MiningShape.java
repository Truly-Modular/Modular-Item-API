package smartin.miapi.modules.properties.mining.shape;

import com.google.gson.JsonObject;
import smartin.miapi.modules.ModuleInstance;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * Mining Shape is supposed to be the original scanner for the blocks.
 * Its the Implementation of the Algorythm for scanning blocks
 */
public interface MiningShape {
    MiningShape fromJson(JsonObject object, ModuleInstance moduleInstance);

    List<BlockPos> getMiningBlocks(Level world, BlockPos pos, Direction face);
}
