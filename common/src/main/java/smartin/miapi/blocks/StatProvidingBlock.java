package smartin.miapi.blocks;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventListener;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.registries.RegistryInventory;

import java.util.function.BiFunction;

public class StatProvidingBlock extends Block implements BlockEntityProvider {
    private final BiFunction<BlockPos, BlockState, ? extends StatProvidingBlockEntity> blockEntityGetter;

    public StatProvidingBlock(Settings settings, BiFunction<BlockPos, BlockState, ? extends StatProvidingBlockEntity> blockEntityGetter) {
        super(settings.pistonBehavior(PistonBehavior.IGNORE));
        this.blockEntityGetter = blockEntityGetter;
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        world.emitGameEvent(RegistryInventory.statProviderUpdatedEvent, pos, new GameEvent.Emitter(null, state));
    }

    @Override
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        world.emitGameEvent(RegistryInventory.statProviderUpdatedEvent, pos, new GameEvent.Emitter(null, null));
    }

    @Override
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        world.emitGameEvent(RegistryInventory.statProviderUpdatedEvent, pos, new GameEvent.Emitter(null, null));
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
