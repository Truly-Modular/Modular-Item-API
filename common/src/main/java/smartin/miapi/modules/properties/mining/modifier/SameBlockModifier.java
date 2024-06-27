package smartin.miapi.modules.properties.mining.modifier;

import com.google.gson.JsonElement;
import smartin.miapi.modules.ModuleInstance;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SameBlockModifier implements MiningModifier {

    public SameBlockModifier() {

    }

    @Override
    public MiningModifier fromJson(JsonElement object, ModuleInstance moduleInstance) {
        return this;
    }

    @Override
    public List<BlockPos> adjustMiningBlock(Level world, BlockPos pos, Player player, ItemStack itemStack, List<BlockPos> blocks) {
        return blocks.stream().filter(blockPos -> world.getBlockState(blockPos).getBlock().equals(world.getBlockState(pos).getBlock())).toList();
    }
}
