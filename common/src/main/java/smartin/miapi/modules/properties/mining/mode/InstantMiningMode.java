package smartin.miapi.modules.properties.mining.mode;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import smartin.miapi.modules.properties.mining.mode.MiningMode;

import java.util.List;

public class InstantMiningMode implements MiningMode {
    @Override
    public MiningMode fromJson(JsonObject object) {
        return this;
    }

    @Override
    public void execute(List<BlockPos> posList, World world, PlayerEntity player) {
        posList.forEach(blockPos -> world.breakBlock(blockPos, !player.isCreative(), player));
    }
}
