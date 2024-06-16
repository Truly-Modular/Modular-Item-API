package smartin.miapi.modules.properties.mining.mode;

import com.google.gson.JsonObject;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;
import smartin.miapi.modules.properties.mining.MiningShapeProperty;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StaggeredMiningMode implements MiningMode {
    public float speed = 1;
    public double durabilityBreakChance;
    public static List<Runnable> nextTickTask = new ArrayList<>();

    static {
        TickEvent.SERVER_POST.register((server -> {
            List<Runnable> currentTicks = new ArrayList<>(nextTickTask);
            nextTickTask.clear();
            currentTicks.forEach(server::execute);
        }));
    }

    public StaggeredMiningMode() {

    }

    @Override
    public MiningMode fromJson(JsonObject object, ModuleInstance moduleInstance) {
        StaggeredMiningMode miningMode = new StaggeredMiningMode();
        miningMode.speed = (float) MiningShapeProperty.getDouble(object, "speed", moduleInstance, 1);
        miningMode.durabilityBreakChance = MiningShapeProperty.getDouble(object, "durability_chance", moduleInstance, 1);
        return miningMode;
    }

    @Override
    public void execute(List<BlockPos> posList, World world, ServerPlayerEntity player, BlockPos origin, ItemStack itemStack) {
        List<BlockPos> reducedList = new ArrayList<>(posList);
        reducedList.sort(Comparator.comparingDouble((pos) -> pos.getSquaredDistance(origin)));
        nextTickTask.add(() -> {
            BlockPos pos;
            int success = 0;
            do {
                pos = reducedList.remove(0);
                if (world.breakBlock(pos, MiningLevelProperty.canMine(world.getBlockState(pos), world, pos, player) && !player.isCreative(), player)) {
                    success++;
                    if(!player.isCreative()){
                        removeDurability(durabilityBreakChance, itemStack, world, player);
                    }
                }
            } while (
                    success < speed
                            && !reducedList.isEmpty() && itemStack.getMaxDamage() - itemStack.getDamage() > 1
            );
            if (!reducedList.isEmpty() && itemStack.getMaxDamage() - itemStack.getDamage() > 1) {
                execute(reducedList, world, player, origin, itemStack);
            }
        });
    }
}
