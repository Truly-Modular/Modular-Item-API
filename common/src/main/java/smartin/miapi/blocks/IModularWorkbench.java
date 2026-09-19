package smartin.miapi.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import smartin.miapi.craft.stat.CraftingStat;

public interface IModularWorkbench {
    BlockPos getBlockPos();

    Level getLevel();

    <T> T getStat(CraftingStat<T> stat);

}
