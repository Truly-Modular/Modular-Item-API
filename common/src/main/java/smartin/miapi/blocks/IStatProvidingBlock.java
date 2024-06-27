package smartin.miapi.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import smartin.miapi.craft.stat.StatProvidersMap;

/**
 * An interface used for blocks which provide stats to {@link ModularWorkBenchEntity ModularWorkBenchEntities}
 */
public interface IStatProvidingBlock {
    StatProvidersMap getProviders(ModularWorkBenchEntity caller, BlockState state, BlockPos pos, ServerLevel world);
}
