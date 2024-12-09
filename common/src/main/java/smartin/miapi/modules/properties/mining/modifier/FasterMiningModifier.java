package smartin.miapi.modules.properties.mining.modifier;

import com.google.gson.JsonElement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import smartin.miapi.modules.ItemModule;

import java.util.List;

public class FasterMiningModifier implements MiningModifier {

    public FasterMiningModifier() {

    }

    @Override
    public MiningModifier fromJson(JsonElement object, ItemModule.ModuleInstance moduleInstance) {
        return this;
    }

    @Override
    public List<BlockPos> adjustMiningBlock(World world, BlockPos pos, PlayerEntity player, ItemStack itemStack, List<BlockPos> blocks) {
        float hardness = world.getBlockState(pos).getBlock().getHardness();

        return blocks.stream().filter(blockPos -> world.getBlockState(blockPos).getBlock().getHardness() <= hardness).toList();
    }
}
