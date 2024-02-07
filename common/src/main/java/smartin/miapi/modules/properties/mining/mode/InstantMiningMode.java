package smartin.miapi.modules.properties.mining.mode;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

import java.util.List;

/**
 * This Mining Mode mines all blocksinstantly and creates the drops where the block was mined
 */
public class InstantMiningMode implements MiningMode {
    @Override
    public MiningMode fromJson(JsonObject object) {
        return this;
    }

    @Override
    public void execute(List<BlockPos> posList, World world, PlayerEntity player) {
        posList.forEach(blockPos -> world.breakBlock(blockPos, MiningLevelProperty.canMine(world.getBlockState(blockPos),world,blockPos,player) && !player.isCreative(), player));
    }
}
