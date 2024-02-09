package smartin.miapi.modules.properties.mining.shape;

import com.google.gson.JsonObject;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.mining.MiningShapeProperty;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class VeinMiningShape implements MiningShape {
    public int size = 5;
    public int maxBlocks = 15;

    @Override
    public MiningShape fromJson(JsonObject object, ItemModule.ModuleInstance moduleInstance) {
        VeinMiningShape veinMiningShape = new VeinMiningShape();
        veinMiningShape.size = MiningShapeProperty.getInteger(object,"size",moduleInstance,5);
        veinMiningShape.maxBlocks = MiningShapeProperty.getInteger(object,"max",moduleInstance,5);
        return veinMiningShape;
    }

    @Override
    public List<BlockPos> getMiningBlocks(World world, BlockPos pos, Direction face) {
        List<BlockPos> miningBlocks = new ArrayList<>();
        Queue<BlockPos> queue = new LinkedList<>();
        List<BlockPos> visited = new ArrayList<>();

        queue.add(pos);
        visited.add(pos);

        BlockState centerState = world.getBlockState(pos);

        while (!queue.isEmpty() && miningBlocks.size() < size * size * size && miningBlocks.size() < maxBlocks) {
            BlockPos currentPos = queue.poll();
            miningBlocks.add(currentPos);

            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = currentPos.offset(direction);

                // Check if neighbor position is within the size limit and hasn't been visited
                int dx = neighborPos.getX() - pos.getX() + size;
                int dy = neighborPos.getY() - pos.getY() + size;
                int dz = neighborPos.getZ() - pos.getZ() + size;
                if (Math.abs(dx - size) <= size && Math.abs(dy - size) <= size && Math.abs(dz - size) <= size
                        && !visited.contains(neighborPos)) {

                    visited.add(neighborPos);

                    BlockState neighborState = world.getBlockState(neighborPos);
                    if (neighborState.getBlock().equals(centerState.getBlock())) {
                        queue.add(neighborPos);
                        Miapi.LOGGER.info("testing " + neighborPos);
                    } else {
                        Miapi.LOGGER.info("not-same " + neighborPos);
                    }
                } else {
                    Miapi.LOGGER.info("failed " + neighborPos + visited.contains(neighborPos));
                }
            }
        }

        return miningBlocks;
    }
}
