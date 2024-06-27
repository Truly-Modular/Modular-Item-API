package smartin.miapi.modules.properties.mining.mode;

import com.google.gson.JsonObject;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;
import smartin.miapi.modules.properties.mining.MiningShapeProperty;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * This Mining Mode mines all blocksinstantly and creates the drops where the block was mined
 */
public class InstantMiningMode implements MiningMode {
    public double durabilityBreakChance;

    public InstantMiningMode(double durabilityBreakChance) {
        this.durabilityBreakChance = durabilityBreakChance;
    }

    @Override
    public MiningMode fromJson(JsonObject object, ModuleInstance moduleInstance) {
        return new InstantMiningMode(MiningShapeProperty.getDouble(object, "durability_chance", moduleInstance, 1));
    }

    @Override
    public void execute(List<BlockPos> posList, Level world, ServerPlayer player, BlockPos origin, ItemStack itemStack) {
        List<BlockPos> reducedList = new ArrayList<>(posList);
        reducedList.sort(Comparator.comparingDouble((pos) -> pos.distSqr(origin)));
        posList.forEach(blockPos -> {
            if (itemStack.getMaxDamage() - itemStack.getDamageValue() > 1 &&
                    world.destroyBlock(blockPos, MiningLevelProperty.canMine(world.getBlockState(blockPos), world, blockPos, player) && !player.isCreative(), player)
            ) {
                if (!player.isCreative()) {
                    removeDurability(durabilityBreakChance, itemStack, world, player);
                }
            }
        });
    }
}
