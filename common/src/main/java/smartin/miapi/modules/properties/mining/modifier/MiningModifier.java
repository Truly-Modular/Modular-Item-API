package smartin.miapi.modules.properties.mining.modifier;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import smartin.miapi.modules.properties.mining.shape.MiningShape;

import java.util.List;

/**
 * This is a post original scan Modifier of the Found Blocks
 * they are meant to filter after the {@link MiningShape} scanned for the block
 * use cautiously
 */
public interface MiningModifier {
    List<BlockPos> adjustMiningBlock(Level world, BlockPos pos, Player player, ItemStack itemStack, List<BlockPos> blocks);

    ResourceLocation getID();
}
