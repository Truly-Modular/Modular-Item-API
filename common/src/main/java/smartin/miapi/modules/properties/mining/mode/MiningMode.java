package smartin.miapi.modules.properties.mining.mode;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import smartin.miapi.modules.properties.mining.modifier.MiningModifier;

import java.util.List;

/**
 * Mining Modes are supposed to control how sth is mined.
 * They control the actual mining Part
 * They are not meant to filter the mining blocks
 * {@link MiningModifier} is meant to filter blocks from the shape
 */
public interface MiningMode {
    MiningMode fromJson(JsonObject object);

    void execute(List<BlockPos> posList, World world, PlayerEntity player);

}
