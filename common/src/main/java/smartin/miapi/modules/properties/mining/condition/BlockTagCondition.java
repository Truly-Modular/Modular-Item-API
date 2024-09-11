package smartin.miapi.modules.properties.mining.condition;

import com.mojang.serialization.MapCodec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import smartin.miapi.Miapi;

import java.util.ArrayList;
import java.util.List;
/**
 * The `BlockTagCondition` class represents a mining condition that allows mining only if the block at a specific position has a tag that matches any of the specified tags.
 * This condition provides a way to filter blocks based on their tags, ensuring that only blocks with the correct tags are affected by the mining shape.
 *
 * @header Block Tag Condition
 * @path /data_types/properties/mining/shape/block_tag
 * @description_start
 * The `BlockTagCondition` implements the `MiningCondition` interface and is used to enforce that mining shapes only apply to blocks that have one of the specified tags.
 * It is useful for scenarios where you want to restrict the mining operation to blocks that are categorized under specific tags.
 * @description_end
 * @path /data_types/properties/mining/condition/block_tag
 * @data block_tag: A list of block tags that are used to determine which blocks are eligible for mining. The mining operation will only apply to blocks with these tags.
 *
 */

public class BlockTagCondition implements MiningCondition {
    public static MapCodec<BlockTagCondition> CODEC = AutoCodec.of(BlockTagCondition.class);
    public static ResourceLocation ID = Miapi.id("block_tag");
    public List<TagKey<Block>> tags;

    public BlockTagCondition(List<TagKey<Block>> blockTags) {
        this.tags = blockTags;
    }

    public BlockTagCondition() {
        this(new ArrayList<>());
    }

    @Override
    public List<BlockPos> trimList(Level level, BlockPos original, List<BlockPos> positions) {
        return positions.stream().filter(pos -> isInAny(level, pos)).toList();
    }

    public boolean isInAny(Level world, BlockPos pos) {
        for (TagKey<Block> tag : tags) {
            if (world.getBlockState(pos).is(tag)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canMine(Player player, Level level, ItemStack miningStack, BlockPos pos, Direction face) {
        return isInAny(level, pos);
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }
}
