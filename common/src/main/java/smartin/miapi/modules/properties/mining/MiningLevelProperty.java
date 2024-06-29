package smartin.miapi.modules.properties.mining;

import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.AttributeProperty;
import smartin.miapi.modules.properties.DurabilityProperty;
import smartin.miapi.modules.properties.enchanment.EnchantAbilityProperty;
import smartin.miapi.modules.properties.ToolOrWeaponProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.security.InvalidParameterException;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

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
        miningCapabilities.put("pickaxe", BlockTags.MINEABLE_WITH_PICKAXE);
        miningCapabilities.put("axe", BlockTags.MINEABLE_WITH_AXE);
        miningCapabilities.put("shovel", BlockTags.MINEABLE_WITH_SHOVEL);
        miningCapabilities.put("hoe", BlockTags.MINEABLE_WITH_HOE);
        miningCapabilities.put("sword", BlockTags.SWORD_EFFICIENT);
        miningLevels.put(BlockTags.NEEDS_STONE_TOOL, 1);
        miningLevels.put(BlockTags.NEEDS_IRON_TOOL, 2);
        miningLevels.put(BlockTags.NEEDS_DIAMOND_TOOL, 3);
        ModularItemCache.setSupplier(KEY, (stack) -> {
            Map<String, Float> mergedMap = new HashMap<>();
            List<ModuleInstance> modules = ItemModule.getModules(stack).allSubModules();
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
    static Map<ItemStack, Tier> toolMaterialLookup = Collections.synchronizedMap(new WeakHashMap<>());

    public static Tier getFakeToolMaterial(ItemStack itemStack) {
        return toolMaterialLookup.getOrDefault(itemStack, getFakeToolMaterialCache(itemStack));
    }

    private static Tier getFakeToolMaterialCache(ItemStack itemStack) {
        return new Tier() {
            @Override
            public int getUses() {
                return (int) DurabilityProperty.property.getValueSafe(itemStack);
            }

            @Override
            public float getSpeed() {
                return getHighestMiningSpeedMultiplier(itemStack);
            }

            @Override
            public float getAttackDamageBonus() {
                return (float) AttributeProperty.getActualValue(itemStack, EquipmentSlot.MAINHAND, Attributes.ATTACK_DAMAGE, 1);
            }

            @Override
            public int getMiningLevel() {
                return getMiningLevelHighest(itemStack);
            }

            @Override
            public int getEnchantmentValue() {
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
            if (state.is(entry.getValue())) {
                Float level = mergedMap.get(entry.getKey());
                if (level != null) {
                    for (Map.Entry<TagKey<Block>, Integer> miningLevelEntry : miningLevels.entrySet()) {
                        if (state.is(miningLevelEntry.getKey())) {
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

    public static boolean canMine(BlockState state, Level world, BlockPos pos, Player miner) {
        ItemStack stack = miner.getUseItem();
        if (ToolOrWeaponProperty.isWeapon(stack)) {
            return !miner.isCreative();
        }
        return true;
    }

    public static float getMiningSpeedMultiplier(ItemStack stack, String type) {
        Multimap<Attribute, AttributeModifier> attributes;
        attributes = AttributeProperty.equipmentSlotMultimapMap(stack).get(EquipmentSlot.MAINHAND);
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
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            double value = AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.MINING_SPEED_PICKAXE, 1);
            return (value == 0) ? 1.0f : (float) value;
        }
        if (state.is(BlockTags.MINEABLE_WITH_AXE)) {
            double value = AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.MINING_SPEED_AXE, 1);
            return (value == 0) ? 1.0f : (float) value;
        }
        if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            double value = AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.MINING_SPEED_SHOVEL, 1);
            return (value == 0) ? 1.0f : (float) value;
        }
        if (state.is(BlockTags.MINEABLE_WITH_HOE)) {
            double value = AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.MINING_SPEED_HOE, 1);
            return (value == 0) ? 1.0f : (float) value;
        }
        if (stack.getItem() instanceof SwordItem) {
            if (state.is(Blocks.COBWEB)) {
                return 15.0F;
            } else {
                return state.is(BlockTags.SWORD_EFFICIENT) ? 1.5F : 1.0F;
            }
        }
        return 1.0f;
    }

    public static float getHighestMiningSpeedMultiplier(ItemStack stack) {
        float start = 0.0f;
        start = (float) Math.max(start, AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.MINING_SPEED_PICKAXE, 1));
        start = (float) Math.max(start, AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.MINING_SPEED_AXE, 1));
        start = (float) Math.max(start, AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.MINING_SPEED_SHOVEL, 1));
        start = (float) Math.max(start, AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.MINING_SPEED_HOE, 1));
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
