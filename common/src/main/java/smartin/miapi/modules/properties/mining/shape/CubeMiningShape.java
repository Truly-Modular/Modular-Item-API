package smartin.miapi.modules.properties.mining.shape;

import com.google.gson.JsonObject;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.mining.MiningShapeProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * This class adds a Cube Mining shape with
 * radius height, width, depth.
 * if height, width, depth are not defined they fallback onto radius
 */
public class CubeMiningShape implements MiningShape {
    int width;
    int height;
    int depth;

    @Override
    public MiningShape fromJson(JsonObject object, ModuleInstance moduleInstance) {
        int radius = MiningShapeProperty.getInteger(object, "radius", 1);
        CubeMiningShape cube = new CubeMiningShape();
        cube.width = MiningShapeProperty.getInteger(object, "width", moduleInstance, radius);
        cube.height = MiningShapeProperty.getInteger(object, "height", moduleInstance, radius);
        cube.depth = MiningShapeProperty.getInteger(object, "depth", moduleInstance, radius);
        return cube;
    }

    @Override
    public List<BlockPos> getMiningBlocks(World world, BlockPos pos, Direction face) {
        List<Direction.Axis> axisList = new ArrayList<>(List.of(Direction.Axis.values()));
        axisList.remove(face.getAxis());
        Direction.Axis widthDirection = axisList.remove(0);
        Direction.Axis heightDirection = axisList.remove(0);
        List<BlockPos> list = new ArrayList<>(depth * height * width);
        for (int x = 0; x < depth; x++) {
            for (int y = 1; y <= width; y++) {
                for (int z = 1; z <= height; z++) {
                    BlockPos pos1 = pos.mutableCopy();
                    pos1 = pos1.add(face.getVector().multiply(-x));
                    pos1 = pos1.offset(widthDirection, intHalfInverse(y));
                    pos1 = pos1.offset(heightDirection, intHalfInverse(z));
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
}
