package smartin.miapi.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import smartin.miapi.craft.stat.StatProvidersMap;

/**
 * An interface used for blocks which provide stats to {@link ModularWorkBenchEntity ModularWorkBenchEntities}
 */
public interface IStatProvidingBlock {
    StatProvidersMap getProviders(ModularWorkBenchEntity caller, BlockState state, BlockPos pos, ServerWorld world);
}
