package smartin.miapi.modules.properties.mining.modifier;

import com.google.gson.JsonElement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.mining.shape.MiningShape;

import java.util.List;

/**
 * This is a post original scan Modifier of the Found Blocks
 * they are meant to filter after the {@link MiningShape} scanned for the block
 * use cautiously
 */
public interface MiningModifier {
    MiningModifier fromJson(JsonElement object, ItemModule.ModuleInstance moduleInstance);

    List<BlockPos> adjustMiningBlock(World world, BlockPos pos, PlayerEntity player, ItemStack itemStack, List<BlockPos> blocks);
}
