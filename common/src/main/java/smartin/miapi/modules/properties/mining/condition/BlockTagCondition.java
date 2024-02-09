package smartin.miapi.modules.properties.mining.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import smartin.miapi.modules.ItemModule;

import java.util.ArrayList;
import java.util.List;

public class BlockTagCondition implements MiningCondition {
    public List<TagKey<Block>> blockTags;

    public BlockTagCondition(List<TagKey<Block>> blockTags) {
        this.blockTags = blockTags;
    }

    @Override
    public MiningCondition fromJson(JsonObject object, ItemModule.ModuleInstance moduleInstance) {
        JsonElement element = object.get("tags");
        List<TagKey<Block>> tags = new ArrayList<>();
        if (element != null && element.isJsonArray()) {
            element.getAsJsonArray().forEach(entry -> {
                tags.add(TagKey.of(RegistryKeys.BLOCK, new Identifier(entry.getAsString())));
            });
        }
        return new BlockTagCondition(tags);
    }

    @Override
    public List<BlockPos> trimList(World level, BlockPos original, List<BlockPos> positions) {
        return positions.stream().filter(pos -> isInAny(level, pos)).toList();
    }

    public boolean isInAny(World world, BlockPos pos) {
        for (TagKey<Block> tag : blockTags) {
            if (world.getBlockState(pos).isIn(tag)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canMine(PlayerEntity player, World level, ItemStack miningStack, BlockPos pos, Direction face) {
        return isInAny(level, pos);
    }
}
