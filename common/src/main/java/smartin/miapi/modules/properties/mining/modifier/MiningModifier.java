package smartin.miapi.modules.properties.mining.modifier;

import com.google.gson.JsonElement;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.mining.shape.MiningShape;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * This is a post original scan Modifier of the Found Blocks
 * they are meant to filter after the {@link MiningShape} scanned for the block
 * use cautiously
 */
public interface MiningModifier {
    MiningModifier fromJson(JsonElement object, ModuleInstance moduleInstance);

    List<BlockPos> adjustMiningBlock(Level world, BlockPos pos, Player player, ItemStack itemStack, List<BlockPos> blocks);
}
