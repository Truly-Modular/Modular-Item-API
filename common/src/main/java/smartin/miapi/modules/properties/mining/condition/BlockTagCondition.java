package smartin.miapi.modules.properties.mining.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import smartin.miapi.modules.ModuleInstance;

import java.util.ArrayList;
import java.util.List;

public class BlockTagCondition implements MiningCondition {
    public List<TagKey<Block>> blockTags;

    public BlockTagCondition(List<TagKey<Block>> blockTags) {
        this.blockTags = blockTags;
    }

    @Override
    public MiningCondition fromJson(JsonObject object, ModuleInstance moduleInstance) {
        JsonElement element = object.get("tags");
        List<TagKey<Block>> tags = new ArrayList<>();
        if (element != null && element.isJsonArray()) {
            element.getAsJsonArray().forEach(entry -> {
                tags.add(TagKey.create(Registries.BLOCK, ResourceLocation.parse(entry.getAsString())));
            });
        }
        return new BlockTagCondition(tags);
    }

    @Override
    public List<BlockPos> trimList(Level level, BlockPos original, List<BlockPos> positions) {
        return positions.stream().filter(pos -> isInAny(level, pos)).toList();
    }

    public boolean isInAny(Level world, BlockPos pos) {
        for (TagKey<Block> tag : blockTags) {
            if (world.getBlockState(pos).is(tag)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canMine(Player player, Level level, ItemStack miningStack, BlockPos pos, Direction face) {
        return isInAny(level, pos);
    }
}
