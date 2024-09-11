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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The `InstantMiningMode` class represents a mining mode where all specified blocks are mined instantly, and the drops are placed where the blocks were mined.
 * This mode is ideal for scenarios where you want to quickly mine a group of blocks and ensure that the drops appear at the original block positions.
 * @header Instant Mining
 * @path /data_types/properties/mining/shape/instant_mining
 * @description_start
 * This mode processes all blocks in the list instantly, creating drops at the exact location where each block was mined. It also includes a durability break chance which determines the likelihood of breaking the item during mining.
 *
 * @data codec: Defines the codec used to serialize and deserialize `InstantMiningMode` instances.
 * @data id: The unique identifier for this mining mode, used in configurations and data definitions.
 * @data durabilityBreakChance: The probability that the item's durability will decrease during mining. This value ranges from 0.0 (no chance) to 1.0 (guaranteed breakage).
 *
 * @description_end
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
                tryBreakBlock(player,blockPos)
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
