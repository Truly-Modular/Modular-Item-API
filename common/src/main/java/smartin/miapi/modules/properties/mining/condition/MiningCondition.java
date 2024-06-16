package smartin.miapi.modules.properties.mining.condition;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import smartin.miapi.modules.ModuleInstance;

import java.util.List;

/**
 * This class adds a Condition for applying the Mining. this is supposed to filter for block or tool prior to mining
 */
public interface MiningCondition {
    MiningCondition fromJson(JsonObject object, ModuleInstance moduleInstance);

    List<BlockPos> trimList(World level, BlockPos original, List<BlockPos> positions);

    boolean canMine(PlayerEntity player, World level, ItemStack miningStack, BlockPos pos, Direction face);

}
