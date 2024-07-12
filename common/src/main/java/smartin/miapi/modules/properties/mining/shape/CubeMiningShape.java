package smartin.miapi.modules.properties.mining.shape;

import com.mojang.serialization.MapCodec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import smartin.miapi.Miapi;

import java.util.ArrayList;
import java.util.List;

/**
 * This class adds a Cube Mining shape with
 * radius height, width, depth.
 * if height, width, depth are not defined they fallback onto radius
 */
public class CubeMiningShape implements MiningShape {
    public static MapCodec<CubeMiningShape> CODEC = AutoCodec.of(CubeMiningShape.class);
    public static ResourceLocation ID = Miapi.id("cube");
    public int width;
    public int height;
    public int depth;

    @Override
    public List<BlockPos> getMiningBlocks(Level world, BlockPos pos, Direction face) {
        List<Direction.Axis> axisList = new ArrayList<>(List.of(Direction.Axis.values()));
        axisList.remove(face.getAxis());
        Direction.Axis widthDirection = axisList.remove(0);
        Direction.Axis heightDirection = axisList.remove(0);
        List<BlockPos> list = new ArrayList<>(depth * height * width);
        for (int x = 0; x < depth; x++) {
            for (int y = 1; y <= width; y++) {
                for (int z = 1; z <= height; z++) {
                    BlockPos pos1 = pos.mutable();
                    pos1 = pos1.offset(face.getNormal().multiply(-x));
                    pos1 = pos1.relative(widthDirection, intHalfInverse(y));
                    pos1 = pos1.relative(heightDirection, intHalfInverse(z));
                    list.add(pos1);
                }
            }
        }
        return list;
    }

    public static int intHalfInverse(int i) {
        if (i % 2 == 0) {
            return i / 2 * (-1);
        }
        return i / 2;
    }

    @Override
    public ResourceLocation getID(){
        return ID;
    }
}
