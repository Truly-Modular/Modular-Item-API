package smartin.miapi.blocks;

import com.mojang.serialization.MapCodec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import net.minecraft.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ModularWorkBench extends BaseEntityBlock {
    private static final VoxelShape BOTTOM = Block.box(2, 0, 4, 14, 4, 12);
    private static final VoxelShape CONNECTOR = Block.box(3, 4, 5, 13, 12, 11);
    private static final VoxelShape BASE = Shapes.or(BOTTOM, CONNECTOR);

    private static final VoxelShape TOP = Block.box(0, 12, 2, 16, 16, 14);
    private static final VoxelShape WHOLE = Shapes.or(BASE, TOP);

    private static final VoxelShape BOTTOM_SWAPPED = Block.box(4, 0, 2, 12, 4, 14);
    private static final VoxelShape CONNECTOR_SWAPPED = Block.box(5, 4, 3, 11, 12, 13);
    private static final VoxelShape BASE_SWAPPED = Shapes.or(BOTTOM_SWAPPED, CONNECTOR_SWAPPED);

    private static final VoxelShape TOP_SWAPPED = Block.box(2, 12, 0, 14, 16, 16);
    private static final VoxelShape WHOLE_SWAPPED = Shapes.or(BASE_SWAPPED, TOP_SWAPPED);

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public ItemStack itemStack;

    public ModularWorkBench(Properties settings) {
        super(settings);
        this.registerDefaultState(((this.stateDefinition.any()).setValue(FACING, Direction.NORTH)));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        //TODO:this probably doesnt work lol
        return AutoCodec.of(ModularWorkBench.class).deprecated(1);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        if (facing.equals(Direction.NORTH) || facing.equals(Direction.SOUTH)) {
            return WHOLE;
        } else {
            return WHOLE_SWAPPED;
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player playerEntity, BlockHitResult blockHitResult) {
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            playerEntity.openMenu(state.getMenuProvider(world, pos));
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.is(newState.getBlock())) return;
        if (world.getBlockEntity(pos) instanceof ModularWorkBenchEntity be)
            if (!be.getItem().isEmpty()) {
                Vec3 vec = pos.getCenter();
                Containers.dropItemStack(world, vec.x, vec.y, vec.z, be.getItem());
            }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof ModularWorkBenchEntity be)
            return be;
        return null;
    }

    @Override
    public <T extends BlockEntity> GameEventListener getListener(ServerLevel world, T blockEntity) {
        if (blockEntity instanceof ModularWorkBenchEntity bench)
            return bench;
        return null;
    }

    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ModularWorkBenchEntity(pos, state);
    }
}
