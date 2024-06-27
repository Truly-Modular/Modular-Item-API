package smartin.miapi.modules.properties.mining.condition;

import com.google.gson.JsonObject;
import smartin.miapi.modules.ModuleInstance;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * This class adds a Condition for applying the Mining. this is supposed to filter for block or tool prior to mining
 */
public interface MiningCondition {
    MiningCondition fromJson(JsonObject object, ModuleInstance moduleInstance);

    List<BlockPos> trimList(Level level, BlockPos original, List<BlockPos> positions);

    boolean canMine(Player player, Level level, ItemStack miningStack, BlockPos pos, Direction face);

}
