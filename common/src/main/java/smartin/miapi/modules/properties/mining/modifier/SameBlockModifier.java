package smartin.miapi.modules.properties.mining.modifier;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import smartin.miapi.Miapi;

import java.util.List;

/**
 * The `SameBlockModifier` class implements a mining modifier that adjusts the list of blocks to mine based on whether they match the type of the initial block.
 * @header Same Block modifier
 * @path /data_types/properties/mining/shape/same_block_modifier
 * @description_start
 * This modifier ensures that only blocks of the same type as the one being initially mined are processed. If the `requireSame` flag is set to true, the modifier will filter the list of blocks to only include those of the same type as the starting block. If set to false, all blocks in the list will be processed.
 * @description_end
 * @data requireSame: A boolean flag indicating whether only blocks of the same type as the initial block should be mined.
 */

public class SameBlockModifier implements MiningModifier {
    public static Codec<SameBlockModifier> CODEC = new Codec<SameBlockModifier>() {
        @Override
        public <T> DataResult<Pair<SameBlockModifier, T>> decode(DynamicOps<T> ops, T input) {
            var result = Miapi.FIXED_BOOL_CODEC.decode(ops, input);
            if (result.isSuccess()) {
                SameBlockModifier sameBlockModifier = new SameBlockModifier();
                sameBlockModifier.requireSame = result.getOrThrow().getFirst();
                return new DataResult.Success<>(new Pair<>(sameBlockModifier, result.getOrThrow().getSecond()), result.lifecycle());
            }
            return DataResult.error(() -> "could not decode SameBlockModifier!");
        }

        @Override
        public <T> DataResult<T> encode(SameBlockModifier input, DynamicOps<T> ops, T prefix) {
            return Miapi.FIXED_BOOL_CODEC.encode(input.requireSame, ops, prefix);
        }
    };
    public static ResourceLocation ID = Miapi.id("require_same");
    @CodecBehavior.Optional
    @AutoCodec.Name("require_same")
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
    public ResourceLocation getID() {
        return ID;
    }
}
