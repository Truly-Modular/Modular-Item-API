package smartin.miapi.modules.properties.mining.shape;

import com.google.gson.JsonObject;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.mining.MiningShapeProperty;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class VeinMiningShape implements MiningShape {
    public int size = 5;
    public int maxBlocks = 15;

    @Override
    public MiningShape fromJson(JsonObject object, ModuleInstance moduleInstance) {
        VeinMiningShape veinMiningShape = new VeinMiningShape();
        veinMiningShape.size = MiningShapeProperty.getInteger(object, "size", moduleInstance, 5);
        veinMiningShape.maxBlocks = MiningShapeProperty.getInteger(object, "max", moduleInstance, 5);
        return veinMiningShape;
    }

    @Override
    public List<BlockPos> getMiningBlocks(Level world, BlockPos pos, Direction face) {
        List<BlockPos> miningBlocks = new ArrayList<>();
        if (maxBlocks < 1) {
            return miningBlocks;
        }
        Queue<BlockPos> queue = new LinkedList<>();
        List<BlockPos> visited = new ArrayList<>();

        queue.add(pos);
        visited.add(pos);

        BlockState centerState = world.getBlockState(pos);

        while (!queue.isEmpty() && miningBlocks.size() < size * size * size && miningBlocks.size() < maxBlocks) {
            BlockPos currentPos = queue.poll();
            miningBlocks.add(currentPos);

            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = currentPos.relative(direction);

                // Check if neighbor position is within the size limit and hasn't been visited
                int dx1 = neighborPos.getX() - pos.getX() + size;
                int dy1 = neighborPos.getY() - pos.getY() + size;
                int dz1 = neighborPos.getZ() - pos.getZ() + size;
                if (Math.abs(dx1 - size) <= size && Math.abs(dy1 - size) <= size && Math.abs(dz1 - size) <= size
                        && !visited.contains(neighborPos)) {

                    visited.add(neighborPos);

                    BlockState neighborState = world.getBlockState(neighborPos);
                    if (neighborState.getBlock().equals(centerState.getBlock())) {
                        queue.add(neighborPos);
                    }
                }
            }
        }

        return miningBlocks;
    }
}
