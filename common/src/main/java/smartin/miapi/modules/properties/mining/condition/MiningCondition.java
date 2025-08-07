package smartin.miapi.modules.properties.mining.condition;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * The `BlockTagCondition` class represents a mining condition that allows mining only if the block at a specific position has a tag that matches any of the specified tags.
 * This condition provides a way to filter blocks based on their tags, ensuring that only blocks with the correct tags are affected by the mining shape.
 *
 * @header Mining Conditions
 * @path /data_types/properties/mining/shape/condition
 * @description_start
 * Conditions allow to restrict what blocks can be mined
 * @description_end
 *
 */
public interface MiningCondition {

    List<BlockPos> trimList(Level level, BlockPos original, List<BlockPos> positions);

    boolean canMine(Player player, Level level, ItemStack miningStack, BlockPos pos, Direction face);

    ResourceLocation getID();

}
