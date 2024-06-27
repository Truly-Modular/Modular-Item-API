package smartin.miapi.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import smartin.miapi.craft.stat.StatProvidersMap;
import smartin.miapi.registries.RegistryInventory;

/**
 * A basic implementation of {@link IStatProvidingBlock}
 */
public class StatProvidingBlock extends Block implements IStatProvidingBlock {
    protected final StatProvidersMap providersMap;

    public StatProvidingBlock(Properties settings, StatProvidersMap providers) {
        super(settings.pushReaction(PushReaction.IGNORE));
        providersMap = providers;
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        world.gameEvent(
                BuiltInRegistries.GAME_EVENT.wrapAsHolder(RegistryInventory.statProviderCreatedEvent),
                pos,
                new GameEvent.Context(null, state));
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) world.gameEvent(
                BuiltInRegistries.GAME_EVENT.wrapAsHolder(RegistryInventory.statProviderCreatedEvent),
                pos,
                new GameEvent.Context(null, state));
    }

    /*@Override
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        world.emitGameEvent(RegistryInventory.statProviderUpdatedEvent, pos, new GameEvent.Emitter(null, null));
    }

    @Override
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        world.emitGameEvent(RegistryInventory.statProviderUpdatedEvent, pos, new GameEvent.Emitter(null, null));
    }*/

    public StatProvidersMap getProviders(ModularWorkBenchEntity caller, BlockState state, BlockPos pos, ServerLevel world) {
        return providersMap;
    }
}
