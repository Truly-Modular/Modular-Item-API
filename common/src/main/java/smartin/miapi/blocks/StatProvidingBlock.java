package smartin.miapi.blocks;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class StatProvidingBlock extends Block implements BlockEntityProvider {
    private final BiFunction<BlockPos, BlockState, ? extends StatProvidingBlockEntity> blockEntityGetter;

    public StatProvidingBlock(Settings settings, BiFunction<BlockPos, BlockState, ? extends StatProvidingBlockEntity> blockEntityGetter) {
        super(settings.pistonBehavior(PistonBehavior.IGNORE));
        this.blockEntityGetter = blockEntityGetter;
    }


    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return blockEntityGetter.apply(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> GameEventListener getGameEventListener(ServerWorld world, T blockEntity) {
        if (blockEntity instanceof StatProvidingBlockEntity be)
            return be;
        return null;
    }
}
