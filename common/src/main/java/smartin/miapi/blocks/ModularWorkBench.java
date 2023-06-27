package smartin.miapi.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
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

    public ModularWorkBench(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return WHOLE;
    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
            return ActionResult.CONSUME;
        }
    }

    @Nullable
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        Text text = Text.literal("test");
        return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> {
            return new CraftingScreenHandler(syncId, inventory);
        }, text);
    }
}
