package smartin.miapi.material.generated;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.block.Block;
import smartin.miapi.loot.MaterialSwapLootFunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TierManager {

    public static final Map<TagKey<Block>, PickaxeItem> TAG_LOOK_UP = new HashMap<>();

    public static void setup() {
        List<PickaxeItem> pickaxeItems = GeneratedMaterialManager.getRegistry().stream()
                .filter(PickaxeItem.class::isInstance)
                .map(PickaxeItem.class::cast)
                .filter(pickaxe -> pickaxe.getTier() != null)
                .filter(pickaxe -> pickaxe.getTier().getIncorrectBlocksForDrops() != null)
                .toList();

        for (PickaxeItem pickaxe : pickaxeItems) {
            TagKey<Block> tagKey = pickaxe.getTier().getIncorrectBlocksForDrops();
            if (tagKey != null) {
                TAG_LOOK_UP.put(tagKey, pickaxe);
            }
        }
    }


    public static int getEstimatedTier(TagKey<Block> incorrecTag) {
        int tier2Level = MaterialSwapLootFunction.getTagSize(BlockTags.INCORRECT_FOR_STONE_TOOL);
        int tier3Level = MaterialSwapLootFunction.getTagSize(BlockTags.INCORRECT_FOR_IRON_TOOL);
        int tier4Level = MaterialSwapLootFunction.getTagSize(BlockTags.INCORRECT_FOR_DIAMOND_TOOL);
        int tier5Level = MaterialSwapLootFunction.getTagSize(BlockTags.INCORRECT_FOR_NETHERITE_TOOL);

        int estimateTag = MaterialSwapLootFunction.getTagSize(incorrecTag);
        if (estimateTag < tier5Level) {
            int tierDiff = tier3Level - tier5Level;
            return 4 + estimateTag / tierDiff;
        } else if (estimateTag < tier4Level) {
            return 4;
        } else if (estimateTag < tier3Level) {
            return 3;
        } else if (estimateTag < tier2Level) {
            return 2;
        }
        return 1;
    }
}
