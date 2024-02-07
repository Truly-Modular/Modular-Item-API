package smartin.miapi.modules.properties.mining.condition;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

public class AlwaysMiningCondition implements MiningCondition {
    @Override
    public MiningCondition fromJson(JsonObject object) {
        return this;
    }

    @Override
    public List<BlockPos> trimList(World level, BlockPos original, List<BlockPos> positions) {
        return positions;
    }

    @Override
    public boolean canMine(PlayerEntity player, World level, ItemStack miningStack, BlockPos pos, Direction face) {
        return true;
    }
}
