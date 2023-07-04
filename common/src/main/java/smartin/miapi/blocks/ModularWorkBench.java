package smartin.miapi.blocks;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;

public class ModularWorkBench extends Block {
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

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
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
        return (BlockState) this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

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

    @Nullable
    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        Text text = Text.literal("test");
        return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> {
            return new CraftingScreenHandler(syncId, inventory);
        }, text);
    }

    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }
}
