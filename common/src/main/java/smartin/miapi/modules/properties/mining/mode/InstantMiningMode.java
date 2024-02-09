package smartin.miapi.modules.properties.mining.mode;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;
import smartin.miapi.modules.properties.mining.MiningShapeProperty;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This Mining Mode mines all blocksinstantly and creates the drops where the block was mined
 */
public class InstantMiningMode implements MiningMode {
    public double durabilityBreakChance;

    public InstantMiningMode(double durabilityBreakChance) {
        this.durabilityBreakChance = durabilityBreakChance;
    }

    @Override
    public MiningMode fromJson(JsonObject object, ItemModule.ModuleInstance moduleInstance) {
        return new InstantMiningMode(MiningShapeProperty.getDouble(object, "durability_chance", moduleInstance, 1));
    }

    @Override
    public void execute(List<BlockPos> posList, World world, ServerPlayerEntity player, BlockPos origin, ItemStack itemStack) {
        List<BlockPos> reducedList = new ArrayList<>(posList);
        reducedList.sort(Comparator.comparingDouble((pos) -> pos.getSquaredDistance(origin)));
        posList.forEach(blockPos -> {
            if (itemStack.getMaxDamage() - itemStack.getDamage() > 1 &&
                    world.breakBlock(blockPos, MiningLevelProperty.canMine(world.getBlockState(blockPos), world, blockPos, player) && !player.isCreative(), player)
            ) {
                if (!player.isCreative()) {
                    removeDurability(durabilityBreakChance, itemStack, world, player);
                }
            }
        });
    }
}
