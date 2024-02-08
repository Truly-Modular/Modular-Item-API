package smartin.miapi.modules.properties.mining.modifier;

import com.google.gson.JsonElement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import smartin.miapi.modules.ModuleInstance;

import java.util.List;

public class SameBlockModifier implements MiningModifier {

    public SameBlockModifier() {

    }

    @Override
    public MiningModifier fromJson(JsonElement object, ModuleInstance moduleInstance) {
        return this;
    }

    @Override
    public List<BlockPos> adjustMiningBlock(World world, BlockPos pos, PlayerEntity player, ItemStack itemStack, List<BlockPos> blocks) {
        return blocks.stream().filter(blockPos -> world.getBlockState(blockPos).getBlock().equals(world.getBlockState(pos).getBlock())).toList();
    }
}
