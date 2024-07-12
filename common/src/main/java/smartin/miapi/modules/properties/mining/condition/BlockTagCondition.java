package smartin.miapi.modules.properties.mining.condition;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class BlockTagCondition implements MiningCondition {
    public static Codec<BlockTagCondition> CODEC = AutoCodec.of(BlockTagCondition.class).codec();
    public List<TagKey<Block>> blockTags;

    public BlockTagCondition(List<TagKey<Block>> blockTags) {
        this.blockTags = blockTags;
    }

    @Override
    public List<BlockPos> trimList(Level level, BlockPos original, List<BlockPos> positions) {
        return positions.stream().filter(pos -> isInAny(level, pos)).toList();
    }

    public boolean isInAny(Level world, BlockPos pos) {
        for (TagKey<Block> tag : blockTags) {
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
}
