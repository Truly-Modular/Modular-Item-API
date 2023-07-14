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
import smartin.miapi.craft.stat.CraftingStat;
import smartin.miapi.registries.RegistryInventory;

public abstract class StatProvidingBlockEntity extends BlockEntity implements GameEventListener {
    public StatProvidingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Creates the map holding the stat instances to set to each defined {@link CraftingStat}.
     * You MUST return a fully wildcard-ed map.
     *
     * @param player the player current who opened the workbench
     * @param pos    the pos of the workbench
     * @param state  the block state at the workbench
     * @param bench  the workbench block entity
     * @return the map of {@link CraftingStat} -> stat instance
     */
    public abstract CraftingStat.Map<?> setupStats(PlayerEntity player, BlockPos pos, BlockState state, ModularWorkBenchEntity bench);

    @Override
    public PositionSource getPositionSource() {
        return new BlockPositionSource(pos);
    }

    @Override
    public boolean listen(ServerWorld world, GameEvent event, GameEvent.Emitter emitter, Vec3d pos) {
        if (event.equals(RegistryInventory.statUpdateEvent)) {
            BlockPos bPos = new BlockPos((int) pos.x, (int) pos.y, (int) pos.z);
            if (
                    !(emitter.sourceEntity() instanceof PlayerEntity player) ||
                    !(world.getBlockEntity(bPos) instanceof ModularWorkBenchEntity bench)
            ) return false;

            CraftingStat.Map.forEach(setupStats(player, bPos, emitter.affectedState(), bench), bench::setBlockStat);

            return true;
        }
        return false;
    }
}
