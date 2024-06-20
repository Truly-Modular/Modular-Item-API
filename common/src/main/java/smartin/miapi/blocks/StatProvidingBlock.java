package smartin.miapi.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import smartin.miapi.craft.stat.StatProvidersMap;
import smartin.miapi.registries.RegistryInventory;

/**
 * A basic implementation of {@link IStatProvidingBlock}
 */
public class StatProvidingBlock extends Block implements IStatProvidingBlock {
    protected final StatProvidersMap providersMap;

    public StatProvidingBlock(Settings settings, StatProvidersMap providers) {
        super(settings.pistonBehavior(PistonBehavior.IGNORE));
        providersMap = providers;
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        world.emitGameEvent(
                Registries.GAME_EVENT.getEntry(RegistryInventory.statProviderCreatedEvent),
                pos,
                new GameEvent.Emitter(null, state));
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) world.emitGameEvent(
                Registries.GAME_EVENT.getEntry(RegistryInventory.statProviderCreatedEvent),
                pos,
                new GameEvent.Emitter(null, state));
    }

    /*@Override
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        world.emitGameEvent(RegistryInventory.statProviderUpdatedEvent, pos, new GameEvent.Emitter(null, null));
    }

    @Override
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        world.emitGameEvent(RegistryInventory.statProviderUpdatedEvent, pos, new GameEvent.Emitter(null, null));
    }*/

    public StatProvidersMap getProviders(ModularWorkBenchEntity caller, BlockState state, BlockPos pos, ServerWorld world) {
        return providersMap;
    }
}
