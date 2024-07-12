package smartin.miapi.modules.properties.mining.modifier;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class SameBlockModifier implements MiningModifier {
    public boolean requireSame;
    public static Codec<SameBlockModifier> CODEC = AutoCodec.of(SameBlockModifier.class).codec();

    @Override
    public List<BlockPos> adjustMiningBlock(Level world, BlockPos pos, Player player, ItemStack itemStack, List<BlockPos> blocks) {
        if (requireSame) {
            return blocks.stream().filter(blockPos -> world.getBlockState(blockPos).getBlock().equals(world.getBlockState(pos).getBlock())).toList();
        } else {
            return blocks;
        }
    }
}
