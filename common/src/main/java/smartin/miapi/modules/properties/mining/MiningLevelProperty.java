package smartin.miapi.modules.properties.mining;

import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.AttributeProperty;
import smartin.miapi.modules.properties.DurabilityProperty;
import smartin.miapi.modules.properties.EnchantAbilityProperty;
import smartin.miapi.modules.properties.ToolOrWeaponProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.security.InvalidParameterException;
import java.util.*;

/**
 * The Property controls mining speed and levels of tools
 */
public class MiningLevelProperty implements ModuleProperty {
    public static MiningLevelProperty property;
    public static final String KEY = "mining_level";
    public static Map<String, TagKey<Block>> miningCapabilities = new HashMap<>();
    public static Map<TagKey<Block>, Integer> miningLevels = new HashMap<>();
    public static Item lastFakedItem;

    public MiningLevelProperty() {
        property = this;
        miningCapabilities.put("pickaxe", BlockTags.PICKAXE_MINEABLE);
        miningCapabilities.put("axe", BlockTags.AXE_MINEABLE);
        miningCapabilities.put("shovel", BlockTags.SHOVEL_MINEABLE);
        miningCapabilities.put("hoe", BlockTags.HOE_MINEABLE);
        miningCapabilities.put("sword", BlockTags.SWORD_EFFICIENT);
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

    public static int getMiningLevel(String type, ItemStack stack) {
        Map<String, Float> mergedMap = ModularItemCache.get(stack, KEY, new HashMap<>());
        Float value = mergedMap.get(type);
        if (value != null) {
            return value.intValue();
        }
        return 0;
    }

    public static int getMiningLevelHighest(ItemStack stack) {
        int highest = 0;
        highest = Math.max(getMiningLevel("axe", stack), highest);
        highest = Math.max(getMiningLevel("pickaxe", stack), highest);
        highest = Math.max(getMiningLevel("shovel", stack), highest);
        highest = Math.max(getMiningLevel("hoe", stack), highest);
        return highest;
    }

    /**
     * we cant use the normal caching since we need to avoid an Itemstack.getItem() call here
     */
    static Map<ItemStack, ToolMaterial> toolMaterialLookup = Collections.synchronizedMap(new WeakHashMap<>());

    public static ToolMaterial getFakeToolMaterial(ItemStack itemStack) {
        return toolMaterialLookup.getOrDefault(itemStack, getFakeToolMaterialCache(itemStack));
    }

    private static ToolMaterial getFakeToolMaterialCache(ItemStack itemStack) {
        return new ToolMaterial() {
            @Override
            public int getDurability() {
                return (int) DurabilityProperty.property.getValueSafe(itemStack);
            }

            @Override
            public float getMiningSpeedMultiplier() {
                return getHighestMiningSpeedMultiplier(itemStack);
            }

            @Override
            public float getAttackDamage() {
                return 1;
            }

            @Override
            public int getMiningLevel() {
                return getMiningLevelHighest(itemStack);
            }

            @Override
            public int getEnchantability() {
                return (int) EnchantAbilityProperty.getEnchantAbility(itemStack);
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.EMPTY;
            }
        };
    }

    public static boolean isSuitable(ItemStack stack, BlockState state) {
        Map<String, Float> mergedMap = ModularItemCache.get(stack, KEY, new HashMap<>());
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

    public static boolean isSuitable(ItemStack stack, String type) {
        if (getMiningLevel(type, stack) > 0) {
            return true;
        }
        if (getMiningSpeedMultiplier(stack, type) > 1) {
            return true;
        }
        return false;
    }

    public static boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        ItemStack stack = miner.getActiveItem();
        if (ToolOrWeaponProperty.isWeapon(stack)) {
            return !miner.isCreative();
        }
        return true;
    }

    public static float getMiningSpeedMultiplier(ItemStack stack, String type) {
        Multimap<EntityAttribute, EntityAttributeModifier> attributes =
                AttributeProperty.equipmentSlotMultimapMap(stack).get(EquipmentSlot.MAINHAND);
        //attributes = stack.getAttributeModifiers(EquipmentSlot.MAINHAND);
        //deprecated actual attribute check because checking attributes to get mining speed is a bad idea for forge mods,
        //as they check the mining speed during attribute resolve, creating a circular deadlock
        switch (type) {
            case "axe": {
                return (float) AttributeProperty.getActualValue(attributes, AttributeRegistry.MINING_SPEED_AXE, 1);
            }
            case "pickaxe": {
                return (float) AttributeProperty.getActualValue(attributes, AttributeRegistry.MINING_SPEED_PICKAXE, 1);
            }
            case "shovel": {
                return (float) AttributeProperty.getActualValue(attributes, AttributeRegistry.MINING_SPEED_SHOVEL, 1);
            }
            case "hoe": {
                return (float) AttributeProperty.getActualValue(attributes, AttributeRegistry.MINING_SPEED_HOE, 1);
            }
            case "sword": {
                return stack.getItem() instanceof SwordItem ? 1.5f : 0;
            }
            default: {
                return 1;
            }
        }
    }

    public static float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        if (state.isIn(BlockTags.PICKAXE_MINEABLE)) {
            double value = AttributeProperty.getActualValueCache(stack, EquipmentSlot.MAINHAND, AttributeRegistry.MINING_SPEED_PICKAXE, 1);
            return (value == 0) ? 1.0f : (float) value;
        }
        if (state.isIn(BlockTags.AXE_MINEABLE)) {
            double value = AttributeProperty.getActualValueCache(stack, EquipmentSlot.MAINHAND, AttributeRegistry.MINING_SPEED_AXE, 1);
            return (value == 0) ? 1.0f : (float) value;
        }
        if (state.isIn(BlockTags.SHOVEL_MINEABLE)) {
            double value = AttributeProperty.getActualValueCache(stack, EquipmentSlot.MAINHAND, AttributeRegistry.MINING_SPEED_SHOVEL, 1);
            return (value == 0) ? 1.0f : (float) value;
        }
        if (state.isIn(BlockTags.HOE_MINEABLE)) {
            double value = AttributeProperty.getActualValueCache(stack, EquipmentSlot.MAINHAND, AttributeRegistry.MINING_SPEED_HOE, 1);
            return (value == 0) ? 1.0f : (float) value;
        }
        if (stack.getItem() instanceof SwordItem) {
            if (state.isOf(Blocks.COBWEB)) {
                return 15.0F;
            } else {
                return state.isIn(BlockTags.SWORD_EFFICIENT) ? 1.5F : 1.0F;
            }
        }
        return 1.0f;
    }

    public static float getHighestMiningSpeedMultiplier(ItemStack stack) {
        float start = 0.0f;
        start = (float) Math.max(start, AttributeProperty.getActualValueCache(stack, EquipmentSlot.MAINHAND, AttributeRegistry.MINING_SPEED_PICKAXE, 1));
        start = (float) Math.max(start, AttributeProperty.getActualValueCache(stack, EquipmentSlot.MAINHAND, AttributeRegistry.MINING_SPEED_AXE, 1));
        start = (float) Math.max(start, AttributeProperty.getActualValueCache(stack, EquipmentSlot.MAINHAND, AttributeRegistry.MINING_SPEED_SHOVEL, 1));
        start = (float) Math.max(start, AttributeProperty.getActualValueCache(stack, EquipmentSlot.MAINHAND, AttributeRegistry.MINING_SPEED_HOE, 1));
        return start;
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
