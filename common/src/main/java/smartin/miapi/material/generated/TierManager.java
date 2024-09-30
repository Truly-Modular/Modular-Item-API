package smartin.miapi.material.generated;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import smartin.miapi.lootFunctions.MaterialSwapLootFunction;

public class TierManager {
    public static int getEstimatedTier(TagKey<Block> incorrecTag) {
        int tier2Level = MaterialSwapLootFunction.getTagSize(BlockTags.INCORRECT_FOR_STONE_TOOL);
        int tier3Level = MaterialSwapLootFunction.getTagSize(BlockTags.INCORRECT_FOR_IRON_TOOL);
        int tier4Level = MaterialSwapLootFunction.getTagSize(BlockTags.INCORRECT_FOR_DIAMOND_TOOL);
        int tier5Level = MaterialSwapLootFunction.getTagSize(BlockTags.INCORRECT_FOR_NETHERITE_TOOL);

        int estimateTag = MaterialSwapLootFunction.getTagSize(incorrecTag);
        if (estimateTag < tier5Level) {
            int tierDiff = tier3Level - tier5Level;
            return 4 + estimateTag/tierDiff;
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
