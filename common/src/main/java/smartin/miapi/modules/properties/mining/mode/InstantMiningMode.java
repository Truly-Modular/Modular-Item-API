package smartin.miapi.modules.properties.mining.mode;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This Mining Mode mines all blocksinstantly and creates the drops where the block was mined
 */
public record InstantMiningMode(double durabilityBreakChance) implements MiningMode {
    public static Codec<InstantMiningMode> CODEC = RecordCodecBuilder.create((miningModeInstance -> {
        return miningModeInstance.group(
                        Codec.DOUBLE
                                .fieldOf("durability_chance")
                                .forGetter(InstantMiningMode::durabilityBreakChance)
                )
                .apply(miningModeInstance, InstantMiningMode::new);
    }));

    @Override
    public void execute(List<BlockPos> posList, Level world, ServerPlayer player, BlockPos origin, ItemStack itemStack) {
        List<BlockPos> reducedList = new ArrayList<>(posList);
        reducedList.sort(Comparator.comparingDouble((pos) -> pos.distSqr(origin)));
        posList.forEach(blockPos -> {
            if (itemStack.getMaxDamage() - itemStack.getDamageValue() > 1 &&
                world.destroyBlock(blockPos,
                        MiningLevelProperty.mineBlock(itemStack, world, world.getBlockState(blockPos), blockPos, player) && !player.isCreative(), player)
            ) {
                if (!player.isCreative()) {
                    removeDurability(durabilityBreakChance, itemStack, world, player);
                }
            }
        });
    }
}
