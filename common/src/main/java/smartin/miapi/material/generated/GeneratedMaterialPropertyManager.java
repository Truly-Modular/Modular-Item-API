package smartin.miapi.material.generated;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import smartin.miapi.Miapi;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.config.MiapiServerConfig;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.abilities.CopyItemAbility;
import smartin.miapi.modules.abilities.util.AbilityMangerProperty;
import smartin.miapi.modules.properties.ComponentProperty;
import smartin.miapi.modules.properties.CopyItemLoreProperty;
import smartin.miapi.modules.properties.FakeItemTagProperty;
import smartin.miapi.modules.properties.attributes.AttributeProperty;
import smartin.miapi.modules.properties.enchanment.CraftingEnchantProperty;
import smartin.miapi.modules.properties.onHit.CopyItemOnHit;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GeneratedMaterialPropertyManager {


    public static Map<String, Map<ModuleProperty<?>, Object>> setup(ResourceLocation id, SwordItem swordItem, DiggerItem axeItem, List<Item> toolMaterials, List<ArmorItem> armorItems, Map<String, Map<ModuleProperty<?>, Object>> old) {
        Map<String, Map<ModuleProperty<?>, Object>> properties = new HashMap<>(old);
        setupProperties(swordItem, id, "blade", Items.DIAMOND_SWORD, Items.WOODEN_SWORD, properties, null);
        setupProperties(axeItem, id, "axe", Items.DIAMOND_AXE, Items.WOODEN_AXE, properties, null);
        setupProperties(toolMaterials.stream().filter(PickaxeItem.class::isInstance).findFirst(), id, "pickaxe", Items.DIAMOND_PICKAXE, Items.WOODEN_PICKAXE, properties, null);
        setupProperties(toolMaterials.stream().filter(ShovelItem.class::isInstance).findFirst(), id, "shovel", Items.DIAMOND_SHOVEL, Items.WOODEN_SHOVEL, properties, null);
        setupProperties(toolMaterials.stream().filter(HoeItem.class::isInstance).findFirst(), id, "hoe", Items.DIAMOND_HOE, Items.WOODEN_HOE, properties, null);
        setupProperties(armorItems.stream()
                .filter(a -> a.getEquipmentSlot() == EquipmentSlot.HEAD).map(a -> (Item) a).findFirst(), id, "helmet", Items.DIAMOND_HELMET, Items.IRON_HELMET, properties, 5);
        setupProperties(armorItems.stream()
                .filter(a -> a.getEquipmentSlot() == EquipmentSlot.CHEST).map(a -> (Item) a).findFirst(), id, "chest", Items.DIAMOND_CHESTPLATE, Items.IRON_CHESTPLATE, properties, 8);
        setupProperties(armorItems.stream()
                .filter(a -> a.getEquipmentSlot() == EquipmentSlot.LEGS).map(a -> (Item) a).findFirst(), id, "pants", Items.DIAMOND_LEGGINGS, Items.IRON_LEGGINGS, properties, 7);
        setupProperties(armorItems.stream()
                .filter(a -> a.getEquipmentSlot() == EquipmentSlot.FEET).map(a -> (Item) a).findFirst(), id, "boot", Items.DIAMOND_BOOTS, Items.IRON_BOOTS, properties, 4);
        return properties;
    }

    private static void setupProperties(Optional<Item> item, ResourceLocation id, String type, Item vanillaCompare, Item vanillaCompare2, Map<String, Map<ModuleProperty<?>, Object>> properties, Integer cost) {
        item.ifPresent(i -> setupProperties(i, id, type, vanillaCompare, vanillaCompare2, properties, cost));
    }

    private static void setupProperties(Item item, ResourceLocation id, String type, Item vanillaCompare, Item vanillaCompare2, Map<String, Map<ModuleProperty<?>, Object>> properties, Integer cost) {
        // Create an intermediate map to accumulate the properties
        Map<ModuleProperty<?>, Object> propertyMap = new HashMap<>();

        if (shouldApplyProperty(MiapiConfig.INSTANCE.server.generatedMaterials.abilityProperty, id.toString())) {
            propertyMap.put(
                    AbilityMangerProperty.property,
                    Map.of(CopyItemAbility.ability, new CopyItemAbility.ItemContext(item))
            );
        }

        if (shouldApplyProperty(MiapiConfig.INSTANCE.server.generatedMaterials.loreProperty, id.toString())) {
            propertyMap.put(
                    CopyItemLoreProperty.property,
                    BuiltInRegistries.ITEM.wrapAsHolder(item));
        }

        if (shouldApplyProperty(MiapiConfig.INSTANCE.server.generatedMaterials.onHitProperty, id.toString())) {
            propertyMap.put(
                    CopyItemOnHit.property,
                    BuiltInRegistries.ITEM.wrapAsHolder(item)
            );
        }

        if (shouldApplyProperty(MiapiConfig.INSTANCE.server.generatedMaterials.tagProperty, id.toString())) {
            Set<TagKey<Item>> tagsCompare1 = vanillaCompare.builtInRegistryHolder().tags().collect(Collectors.toSet());
            Set<TagKey<Item>> tagsCompare2 = vanillaCompare2.builtInRegistryHolder().tags().collect(Collectors.toSet());
            // Find the common tags
            Set<TagKey<Item>> commonTags = tagsCompare1.stream()
                    .filter(tagsCompare2::contains)  // filter tags present in both
                    .collect(Collectors.toSet());

            // Get the tags of the item to compare
            Stream<TagKey<Item>> itemTags = item.builtInRegistryHolder().tags();

            // Find the tags that are in item but not in the commonTags set
            List<String> uniqueTags = itemTags
                    .filter(tag -> !commonTags.contains(tag))
                    .map(tag -> tag.location().toString())
                    .toList();

            propertyMap.put(FakeItemTagProperty.property, uniqueTags);
        }

        if (shouldApplyProperty(MiapiConfig.INSTANCE.server.generatedMaterials.componentProperty, id.toString())) {
            // Get the tags from both vanillaCompare and vanillaCompare2
            var componentTypes = vanillaCompare.components().stream().map(TypedDataComponent::type).collect(Collectors.toSet());
            var commonComponents = vanillaCompare2.components().stream().map(TypedDataComponent::type).filter(componentTypes::contains).collect(Collectors.toSet());

            Map<ResourceLocation, JsonElement> components = new HashMap<>();
            item.components().forEach(typedDataComponent -> {
                if (!commonComponents.contains(typedDataComponent.type())) {
                    ResourceLocation componentId = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(typedDataComponent.type());
                    JsonElement element = encode(typedDataComponent);
                    components.put(componentId, element);
                }
            });
            propertyMap.put(ComponentProperty.property, components);
        }

        if (shouldApplyProperty(MiapiConfig.INSTANCE.server.generatedMaterials.enchantProperty, id.toString())) {
            Map<Holder<Enchantment>, DoubleOperationResolvable> enchantments = new HashMap<>();
            ItemEnchantments itemEnchantments = getDefaultStack(item).getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            itemEnchantments.keySet().forEach(enchantment -> {
                DoubleOperationResolvable resolvable = new DoubleOperationResolvable(List.of(new DoubleOperationResolvable.Operation(itemEnchantments.getLevel(enchantment), AttributeModifier.Operation.ADD_VALUE)));
                enchantments.put(enchantment, resolvable);
            });
            propertyMap.put(CraftingEnchantProperty.property, enchantments);
        }

        if (shouldApplyProperty(MiapiConfig.INSTANCE.server.generatedMaterials.attributeProperty, id.toString())) {
            // Get the tags from both vanillaCompare and vanillaCompare2
            var firstAttributes = getDefaultStack(vanillaCompare).get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers().stream().map(ItemAttributeModifiers.Entry::attribute).toList();
            var secondAttributes = getDefaultStack(vanillaCompare2).get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers().stream().map(ItemAttributeModifiers.Entry::attribute).toList();

            var modifiers = getDefaultStack(item).get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers()
                    .stream()
                    .filter(a -> !firstAttributes.contains(a.attribute()) && !secondAttributes.contains(a.attribute()))
                    .filter(a -> a.modifier().operation() == AttributeModifier.Operation.ADD_VALUE)
                    .toList();

            List<AttributeProperty.AttributeJson> jsonList = modifiers.stream().map(e -> {
                var json = new AttributeProperty.AttributeJson();
                json.targetOperation = "+";
                json.operation = "+";
                json.attribute = e.attribute().getRegisteredName();
                if (cost != null) {
                    json.value = new StatResolver.DoubleFromStat("" + e.modifier().amount() + "/" + cost + " * [module.cost]");
                } else {
                    json.value = new StatResolver.DoubleFromStat(e.modifier().amount());
                }
                json.slot = e.slot();
                Miapi.LOGGER.warn(type);
                Miapi.LOGGER.warn(item.getDescriptionId());
                Miapi.LOGGER.warn(Miapi.gson.toJson(json));
                return json;
                //TODO: for some reason this does NOT work. i dont know
            }).toList();

            propertyMap.put(AttributeProperty.property, jsonList);
        }

        // Add the collected propertyMap to the properties map
        properties.put(type, propertyMap);
    }

    public static boolean shouldApplyProperty(
            MiapiServerConfig.GeneratedMaterialsCategory.GeneratePropertyOption config, String idString) {

        // If the toggle is false, don't apply the property
        if (!config.enable) {
            return false;
        }

        // Check if idString matches any of the blocked regex patterns
        for (String regex : config.blocked) {
            if (idString.matches(regex)) {
                return false; // Blocked by regex, don't apply the property
            }
        }

        return true; // Passes all checks, apply the property
    }

    public static ItemStack getDefaultStack(Item item) {
        ItemStack itemStack = item.getDefaultInstance();
        try {
            //attempt to load betterx itemstack stuffs to grab enchants and other stuffs reliably
            Class<?> itemStackHelperClass = Class.forName("org.betterx.wover.item.api.ItemStackHelper");
            Method method = itemStackHelperClass.getDeclaredMethod("callItemStackSetupIfPossible", ItemStack.class, HolderLookup.Provider.class);
            itemStack = (ItemStack) method.invoke(null, itemStack, Miapi.registryAccess);
        } catch (Exception ignored) {
        }
        return LootItemFunctions.IDENTITY.apply(itemStack, null);
    }

    public static <T> JsonElement encode(TypedDataComponent<T> typedDataComponent) {
        DataComponentType<T> componentType = typedDataComponent.type();
        T data = typedDataComponent.value();
        return componentType.codec().encodeStart(JsonOps.INSTANCE, data).getOrThrow();
    }
}
