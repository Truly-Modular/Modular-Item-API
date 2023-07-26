package smartin.miapi.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.BlockPositionSource;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.craft.stat.CraftingStat;
import smartin.miapi.registries.RegistryInventory;

public abstract class StatProvidingBlockEntity extends BlockEntity implements GameEventListener {
    protected BlockPositionSource blockPositionSource;

    public StatProvidingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Creates the map holding the stat instances to set to each defined {@link CraftingStat}.
     * You MUST return a fully wildcard-ed map.
     *
     * @param pos    the pos of the workbench
     * @param state  the block state at the workbench
     * @param bench  the workbench block entity
     * @return the map of {@link CraftingStat} -> stat instance, or null if you don't want to provide any stats.
     */
    public abstract CraftingStat.StatMap<?> setupStats(BlockPos pos, BlockState state, ModularWorkBenchEntity bench);

    @Override
    public PositionSource getPositionSource() {
        if (blockPositionSource == null) blockPositionSource = new BlockPositionSource(pos);
        return blockPositionSource;
    }

    @Override
    public boolean listen(ServerWorld world, GameEvent event, GameEvent.Emitter emitter, Vec3d pos) {
        if (event.equals(RegistryInventory.statUpdateEvent)) {
            BlockPos bPos = new BlockPos((int) (pos.x-0.5), (int) (pos.y-0.5), (int) (pos.z-0.5));
            if (!(world.getBlockEntity(bPos) instanceof ModularWorkBenchEntity bench)) return false;

            CraftingStat.StatMap.forEach(setupStats(bPos, emitter.affectedState(), bench), bench::setBlockStat);

            return true;
        }
        return false;
    }

    public static class Example extends StatProvidingBlockEntity {
        public Example(BlockPos pos, BlockState state) {
            super(RegistryInventory.exampleStatProviderBlockEntityType, pos, state);
        }

        @Override
        public CraftingStat.StatMap<?> setupStats(BlockPos pos, BlockState state, ModularWorkBenchEntity bench) {
            return new CraftingStat.StatMap<>()
                    .set(RegistryInventory.exampleCraftingStat, 3d);
        }

        @Override
        public int getRange() {
            return 8;
        }
    }
}
