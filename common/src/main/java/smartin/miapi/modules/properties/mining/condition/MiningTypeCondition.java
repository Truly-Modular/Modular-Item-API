package smartin.miapi.modules.properties.mining.condition;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

import java.util.List;

public class MiningTypeCondition implements MiningCondition {
    public String type;

    public MiningTypeCondition(String type) {
        this.type = type;
    }

    @Override
    public MiningCondition fromJson(JsonObject object, ItemModule.ModuleInstance moduleInstance) {
        return new MiningTypeCondition(type);
    }

    @Override
    public List<BlockPos> trimList(World level, BlockPos original, List<BlockPos> positions) {
        return positions.stream().filter(pos -> level.getBlockState(pos).isIn(MiningLevelProperty.miningCapabilities.get(type))).toList();
    }

    @Override
    public boolean canMine(PlayerEntity player, World level, ItemStack miningStack, BlockPos pos, Direction face) {
        return level.getBlockState(pos).isIn(MiningLevelProperty.miningCapabilities.get(type));
    }
}
