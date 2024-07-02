package smartin.miapi.modules.properties.mining;

import com.google.gson.JsonElement;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import smartin.miapi.modules.properties.ToolOrWeaponProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

/**
 * The Property controls mining speed and levels of tools
 */
public class MiningLevelProperty implements ModuleProperty {
    public static MiningLevelProperty property;
    public static final String KEY = "mining_level";
    public static Map<String, TagKey<Block>> miningCapabilities = new HashMap<>();

    public MiningLevelProperty() {
        property = this;
        miningCapabilities.put("pickaxe", BlockTags.MINEABLE_WITH_PICKAXE);
        miningCapabilities.put("axe", BlockTags.MINEABLE_WITH_AXE);
        miningCapabilities.put("shovel", BlockTags.MINEABLE_WITH_SHOVEL);
        miningCapabilities.put("hoe", BlockTags.MINEABLE_WITH_HOE);
        miningCapabilities.put("sword", BlockTags.SWORD_EFFICIENT);
    }



    public static boolean canMine(BlockState state, Level world, BlockPos pos, Player miner) {
        ItemStack stack = miner.getUseItem();
        if (ToolOrWeaponProperty.isWeapon(stack)) {
            return !miner.isCreative();
        }
        return true;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        Map<String, JsonElement> matchedElements = getElements(data);

        if (matchedElements.isEmpty()) {
            throw new InvalidParameterException("At least one Mining type is required");
        }

        return true;
    }

    private Map<String, JsonElement> getElements(JsonElement element) {
        Map<String, JsonElement> matchedElements = new HashMap<>();

        // Iterate through the keys of miningCapabilites map
        if (element != null) {
            for (String key : miningCapabilities.keySet()) {
                if (element.getAsJsonObject().has(key)) {
                    // If the key exists in the JSON, add it to the matchedElements map
                    matchedElements.put(key, element.getAsJsonObject().get(key));
                }
            }
        }
        return matchedElements;
    }


    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        return ModuleProperty.super.merge(old, toMerge, type);
    }
}
