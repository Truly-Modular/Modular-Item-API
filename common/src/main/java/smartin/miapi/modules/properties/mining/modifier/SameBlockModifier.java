package smartin.miapi.modules.properties.mining.modifier;

import com.mojang.serialization.MapCodec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import smartin.miapi.Miapi;

import java.util.List;

public class SameBlockModifier implements MiningModifier {
    public static MapCodec<SameBlockModifier> CODEC = AutoCodec.of(SameBlockModifier.class);
    public static ResourceLocation ID = Miapi.id("same_block");
    public boolean requireSame;

    @Override
    public List<BlockPos> adjustMiningBlock(Level world, BlockPos pos, Player player, ItemStack itemStack, List<BlockPos> blocks) {
        if (requireSame) {
            return blocks.stream().filter(blockPos -> world.getBlockState(blockPos).getBlock().equals(world.getBlockState(pos).getBlock())).toList();
        } else {
            return blocks;
        }
    }

    @Override
    public ResourceLocation getID(){
        return ID;
    }
}
