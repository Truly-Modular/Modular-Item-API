package smartin.miapi.modules.properties.mining.mode;

import com.mojang.serialization.MapCodec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This Mining Mode mines all blocksinstantly and creates the drops where the block was mined
 */
public class InstantMiningMode implements MiningMode {
    public static MapCodec<InstantMiningMode> CODEC = AutoCodec.of(InstantMiningMode.class);
    public static ResourceLocation ID = Miapi.id("instant");

    @CodecBehavior.Optional
    @AutoCodec.Name("durability_break_chance")
    public double durabilityBreakChance = 1.0;

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

    @Override
    public ResourceLocation getID(){
        return ID;
    }
}
