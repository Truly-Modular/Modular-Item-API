package smartin.miapi.material.generated;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.block.Block;
import smartin.miapi.loot.MaterialSwapLootFunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TierManager {

    private static final Map<TagKey<Block>, List<PickaxeItem>> TAG_LOOK_UP = new HashMap<>();

    public static void setup() {
        TAG_LOOK_UP.clear();

        List<PickaxeItem> pickaxeItems = GeneratedMaterialManager.getRegistry().stream()
                .filter(PickaxeItem.class::isInstance)
                .map(PickaxeItem.class::cast)
                .filter(p -> p.getTier() != null && p.getTier().getIncorrectBlocksForDrops() != null)
                .toList();

        TAG_LOOK_UP.putAll(
                pickaxeItems.stream()
                        .collect(Collectors.groupingBy(
                                p -> p.getTier().getIncorrectBlocksForDrops()
                        ))
        );
    }

    /**
     * Returns the preferred pickaxe for this tag, preferring Mojang tools.
     */
    public static Optional<PickaxeItem> getPreferredPickaxe(TagKey<Block> tag) {
        List<PickaxeItem> candidates = TAG_LOOK_UP.get(tag);
        if (candidates == null || candidates.isEmpty()) {
            return Optional.empty();
        }

        // prefer namespace "minecraft"
        return candidates.stream()
                .sorted((a, b) -> {
                    boolean aVanilla = isMojangItem(a);
                    boolean bVanilla = isMojangItem(b);
                    if (aVanilla && !bVanilla) return -1;
                    if (!aVanilla && bVanilla) return 1;
                    return a.getName(a.getDefaultInstance()).getString()
                            .compareToIgnoreCase(b.getName(b.getDefaultInstance()).getString());
                })
                .findFirst();
    }

    public static List<PickaxeItem> getAllPickaxes(TagKey<Block> tag) {
        return TAG_LOOK_UP.getOrDefault(tag, List.of());
    }

    private static boolean isMojangItem(PickaxeItem item) {
        // for 1.20+, use item.builtInRegistryHolder().key().location().getNamespace()
        return item.arch$registryName() != null
               && "minecraft".equals(item.arch$registryName().getNamespace());
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
