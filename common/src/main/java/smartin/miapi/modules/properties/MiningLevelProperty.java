package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MiningLevelProperty implements ModuleProperty {
    public static MiningLevelProperty property;
    public static final String KEY = "mining_level";
    public static Map<String, TagKey<Block>> miningCapabilities = new HashMap<>();
    public static Map<TagKey<Block>, Integer> miningLevels = new HashMap<>();

    public MiningLevelProperty() {
        property = this;
        miningCapabilities.put("pickaxe", BlockTags.PICKAXE_MINEABLE);
        miningCapabilities.put("axe", BlockTags.AXE_MINEABLE);
        miningCapabilities.put("shovel", BlockTags.SHOVEL_MINEABLE);
        miningCapabilities.put("hoe", BlockTags.HOE_MINEABLE);
        miningLevels.put(BlockTags.NEEDS_STONE_TOOL, 1);
        miningLevels.put(BlockTags.NEEDS_IRON_TOOL, 2);
        miningLevels.put(BlockTags.NEEDS_DIAMOND_TOOL, 3);
        ModularItemCache.setSupplier(KEY, (stack) -> {
            Map<String, Float> mergedMap = new HashMap<>();
            List<ItemModule.ModuleInstance> modules = ItemModule.getModules(stack).allSubModules();
            modules.forEach(module -> {
                JsonElement element = module.getProperties().get(property);
                if (element != null) {
                    Map<String, JsonElement> map = getElements(element);
                    map.forEach((id, jsonElement) -> {
                        float value = (float) StatResolver.resolveDouble(jsonElement, module);
                        boolean contains = mergedMap.containsKey(id);
                        if (!contains || contains && mergedMap.get(id) < value) {
                            mergedMap.put(id, value);
                        }
                    });
                }
            });
            return mergedMap;
        });
    }

    public static boolean IsSuitable(ItemStack stack, BlockState state) {
        Map<String, Float> mergedMap = (Map<String, Float>) ModularItemCache.get(stack, KEY);
        for (Map.Entry<String, TagKey<Block>> entry : miningCapabilities.entrySet()) {
            if (state.isIn(entry.getValue())) {
                Float level = mergedMap.get(entry.getKey());
                if (level != null) {
                    for (Map.Entry<TagKey<Block>, Integer> miningLevelEntry : miningLevels.entrySet()) {
                        if (state.isIn(miningLevelEntry.getKey())) {
                            return miningLevelEntry.getValue() <= level;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        ItemStack stack = miner.getActiveItem();
        if(ToolOrWeaponProperty.isWeapon(stack)){
            return !miner.isCreative();
        }
        return true;
    }

    public static float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        if (state.isIn(BlockTags.PICKAXE_MINEABLE)) {
            double value = AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.MINING_SPEED_PICKAXE);
            Miapi.LOGGER.error(String.valueOf(value));
            return (float) value;
        }
        if (state.isIn(BlockTags.AXE_MINEABLE)) {
            double value = AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.MINING_SPEED_AXE);
            return (float) value;
        }
        if (state.isIn(BlockTags.SHOVEL_MINEABLE)) {
            double value = AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.MINING_SPEED_SHOVEL);
            return (float) value;
        }
        if (state.isIn(BlockTags.HOE_MINEABLE)) {
            double value = AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.MINING_SPEED_HOE);
            return (float) value;
        }
        return 1.0f;
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
