package smartin.miapi.modules.properties.mining.shape;

import com.mojang.serialization.MapCodec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class VeinMiningShape implements MiningShape {
    public static MapCodec<VeinMiningShape> CODEC = AutoCodec.of(VeinMiningShape.class);
    public static ResourceLocation ID = Miapi.id("vein");

    public int size = 5;
    @CodecBehavior.Optional
    public DoubleOperationResolvable max = new DoubleOperationResolvable(15);


    @Override
    public List<BlockPos> getMiningBlocks(Level world, BlockPos pos, Direction face) {
        List<BlockPos> miningBlocks = new ArrayList<>();
        if (max.getValue() < 1) {
            return miningBlocks;
        }
        Queue<BlockPos> queue = new LinkedList<>();
        List<BlockPos> visited = new ArrayList<>();

        queue.add(pos);
        visited.add(pos);

        BlockState centerState = world.getBlockState(pos);

        while (!queue.isEmpty() && miningBlocks.size() < size * size * size && miningBlocks.size() < max.getValue()) {
            BlockPos currentPos = queue.poll();
            miningBlocks.add(currentPos);

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue; // Skip the center block itself

                        BlockPos neighborPos = currentPos.offset(dx, dy, dz);

                        // Same bounds and visited checks
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
            }

        }

        return miningBlocks;
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public MiningShape initialize(MiningShape property, ModuleInstance context) {
        VeinMiningShape miningShape = new VeinMiningShape();
        miningShape.max = ((VeinMiningShape) property).max.initialize(context);
        miningShape.size =((VeinMiningShape) property).size;
        return miningShape;
    }
}
