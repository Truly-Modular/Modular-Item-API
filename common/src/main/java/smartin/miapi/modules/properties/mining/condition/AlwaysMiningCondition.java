package smartin.miapi.modules.properties.mining.condition;

import com.mojang.serialization.MapCodec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import smartin.miapi.Miapi;

import java.util.List;

public class AlwaysMiningCondition implements MiningCondition {
    public static MapCodec<AlwaysMiningCondition> CODEC = AutoCodec.of(AlwaysMiningCondition.class);
    public static ResourceLocation ID = Miapi.id("always");

    @Override
    public List<BlockPos> trimList(Level level, BlockPos original, List<BlockPos> positions) {
        return positions;
    }

    @Override
    public boolean canMine(Player player, Level level, ItemStack miningStack, BlockPos pos, Direction face) {
        return true;
    }

    @Override
    public ResourceLocation getID(){
        return ID;
    }
}
