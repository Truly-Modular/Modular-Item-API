package smartin.miapi.modules.properties.mining.shape;

import com.mojang.serialization.MapCodec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

import java.util.ArrayList;
import java.util.List;

/**
 * This class adds a Cube Mining shape with
 * radius height, width, depth.
 * if height, width, depth are not defined they fallback onto radius
 * @header Cube Shape
 * @path /data_types/properties/mining/shape/shapes/cube
 * @description_start
 * A simple Cube Mining shape
 * @description_end
 * @data width: The width of the Cube.
 * @data height: The height of the Cube.
 * @data depth: The depth of the Cube.
 */
public class CubeMiningShape implements MiningShape {
    public static MapCodec<CubeMiningShape> CODEC = AutoCodec.of(CubeMiningShape.class);
    public static ResourceLocation ID = Miapi.id("cube");
    @CodecBehavior.Optional
    public DoubleOperationResolvable width = new DoubleOperationResolvable(1);
    @CodecBehavior.Optional
    public DoubleOperationResolvable height = new DoubleOperationResolvable(1);
    @CodecBehavior.Optional
    public DoubleOperationResolvable depth = new DoubleOperationResolvable(1);

    @Override
    public List<BlockPos> getMiningBlocks(Level world, BlockPos pos, Direction face) {
        List<Direction.Axis> axisList = new ArrayList<>(List.of(Direction.Axis.values()));
        axisList.remove(face.getAxis());
        Direction.Axis widthDirection = axisList.remove(0);
        Direction.Axis heightDirection = axisList.remove(0);
        List<BlockPos> list = new ArrayList<>((int) depth.getValue() * (int) height.getValue() * (int) width.getValue());
        for (int x = 0; x < (int) depth.getValue(); x++) {
            for (int y = 1; y <= (int) width.getValue(); y++) {
                for (int z = 1; z <= (int) height.getValue(); z++) {
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
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public MiningShape initialize(MiningShape property, ModuleInstance context) {
        //TODO:swap to Double resovlable
        CubeMiningShape shape = new CubeMiningShape();
        shape.width = ((CubeMiningShape) property).width.initialize(context);
        shape.depth = ((CubeMiningShape) property).depth.initialize(context);
        shape.height = ((CubeMiningShape) property).height.initialize(context);
        return shape;
    }
}
