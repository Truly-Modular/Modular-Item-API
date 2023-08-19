package smartin.miapi.blocks;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;

public class ModularWorkBench extends BlockWithEntity {
    private static final VoxelShape BOTTOM = Block.createCuboidShape(2, 0, 4, 14, 3, 12);
    private static final VoxelShape CONNECTOR = Block.createCuboidShape(3, 3, 5, 13, 8, 11);
    private static final VoxelShape BASE = VoxelShapes.union(BOTTOM, CONNECTOR);
    private static final VoxelShape TOP = Block.createCuboidShape(0, 8, 2, 16, 11, 14);
    private static final VoxelShape WHOLE = VoxelShapes.union(BASE, TOP);

    private static final VoxelShape BOTTOM_SWAPPED = Block.createCuboidShape(4, 0, 2, 12, 3, 14);
    private static final VoxelShape CONNECTOR_SWAPPED = Block.createCuboidShape(5, 3, 3, 11, 8, 13);
    private static final VoxelShape BASE_SWAPPED = VoxelShapes.union(BOTTOM_SWAPPED, CONNECTOR_SWAPPED);
    private static final VoxelShape TOP_SWAPPED = Block.createCuboidShape(2, 8, 0, 14, 11, 16);
    private static final VoxelShape WHOLE_SWAPPED = VoxelShapes.union(BASE_SWAPPED, TOP_SWAPPED);

    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    public ModularWorkBench(Settings settings) {
        super(settings);
        this.setDefaultState(((this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return false;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction facing = state.get(FACING);
        if(facing.equals(Direction.NORTH) || facing.equals(Direction.SOUTH)){
            return WHOLE;
        }
        else{
            return WHOLE_SWAPPED;
        }
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
            return ActionResult.CONSUME;
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.isOf(newState.getBlock())) return;
        if (world.getBlockEntity(pos) instanceof ModularWorkBenchEntity be)
            if (!be.getItem().isEmpty()) {
                Vec3d vec = pos.toCenterPos();
                ItemScatterer.spawn(world, vec.x, vec.y, vec.z, be.getItem());
            }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof ModularWorkBenchEntity be)
            return be;
        return null;
    }

    @Override
    public <T extends BlockEntity> GameEventListener getGameEventListener(ServerWorld world, T blockEntity) {
        if (blockEntity instanceof ModularWorkBenchEntity bench)
            return bench;
        return null;
    }

    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ModularWorkBenchEntity(pos, state);
    }
}
