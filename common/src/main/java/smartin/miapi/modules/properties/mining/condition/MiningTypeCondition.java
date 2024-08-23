package smartin.miapi.modules.properties.mining.condition;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

import java.util.List;

@Deprecated
//this prob should be removed altogheter
public class MiningTypeCondition implements MiningCondition {
    public static Codec<MiningTypeCondition> CODEC = AutoCodec.of(MiningTypeCondition.class).codec();
    public String type;

    public MiningTypeCondition(String type) {
        this.type = type;
    }

    public MiningTypeCondition() {
        this("empty");
    }

    @Override
    public List<BlockPos> trimList(Level level, BlockPos original, List<BlockPos> positions) {
        return positions.stream().filter(pos -> level.getBlockState(pos).is(MiningLevelProperty.miningCapabilities.get(type))).toList();
    }

    @Override
    public boolean canMine(Player player, Level level, ItemStack miningStack, BlockPos pos, Direction face) {
        return level.getBlockState(pos).is(MiningLevelProperty.miningCapabilities.get(type));
    }

    @Override
    public ResourceLocation getID() {
        return null;
    }
}
