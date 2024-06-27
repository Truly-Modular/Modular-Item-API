package smartin.miapi.modules.properties.mining.condition;

import com.google.gson.JsonObject;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MiningTypeCondition implements MiningCondition {
    public String type;

    public MiningTypeCondition(String type) {
        this.type = type;
    }

    @Override
    public MiningCondition fromJson(JsonObject object, ModuleInstance moduleInstance) {
        return new MiningTypeCondition(type);
    }

    @Override
    public List<BlockPos> trimList(Level level, BlockPos original, List<BlockPos> positions) {
        return positions.stream().filter(pos -> level.getBlockState(pos).is(MiningLevelProperty.miningCapabilities.get(type))).toList();
    }

    @Override
    public boolean canMine(Player player, Level level, ItemStack miningStack, BlockPos pos, Direction face) {
        return level.getBlockState(pos).is(MiningLevelProperty.miningCapabilities.get(type));
    }
}
