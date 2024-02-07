package smartin.miapi.modules.properties.mining.mode;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StaggeredMiningMode implements MiningMode {
    public float speed = 1;
    public static List<Runnable> nextTickTask = new ArrayList<>();

    static {
        ServerTickEvents.START_SERVER_TICK.register((server -> {
            List<Runnable> currentTicks = new ArrayList<>(nextTickTask);
            nextTickTask.clear();
            currentTicks.forEach(server::execute);
        }));
    }

    public StaggeredMiningMode() {

    }

    @Override
    public MiningMode fromJson(JsonObject object) {
        StaggeredMiningMode miningMode = new StaggeredMiningMode();
        if(object.has("float")){
            miningMode.speed = object.get("speed").getAsFloat();
        }
        return miningMode;
    }

    @Override
    public void execute(List<BlockPos> posList, World world, PlayerEntity player) {
        List<BlockPos> reducedList = new ArrayList<>(posList);
        reducedList.sort(Comparator.comparingDouble((pos) -> pos.getSquaredDistance(player.getPos())));
        nextTickTask.add(() -> {
            BlockPos pos = reducedList.remove(0);
            world.breakBlock(pos, MiningLevelProperty.canMine(world.getBlockState(pos),world,pos,player) && !player.isCreative(), player);
            if (!reducedList.isEmpty()) {
                execute(reducedList, world, player);
            }
        });
    }
}
