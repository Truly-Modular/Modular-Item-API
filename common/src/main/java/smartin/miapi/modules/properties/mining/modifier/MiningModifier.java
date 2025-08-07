package smartin.miapi.modules.properties.mining.modifier;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import smartin.miapi.modules.properties.mining.shape.MiningShape;
import smartin.miapi.modules.properties.util.InitializeAble;

import java.util.List;

/**
 * This is a post original scan Modifier of the Found Blocks
 * they are meant to filter after the {@link MiningShape} scanned for the block
 * use cautiously
 * @header Mining Modifiers
 * @path /data_types/properties/mining/shape/modifier
 * @description_start
 * modifies the list of blocks to be mined after the shape is set.
 * @description_end
 */
public interface MiningModifier extends InitializeAble<MiningModifier> {
    List<BlockPos> adjustMiningBlock(Level world, BlockPos pos, Player player, ItemStack itemStack, List<BlockPos> blocks);

    ResourceLocation getID();
}
