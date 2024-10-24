package smartin.miapi.material.generated;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.config.MiapiServerConfig;
import smartin.miapi.modules.abilities.CopyItemAbility;
import smartin.miapi.modules.abilities.util.AbilityMangerProperty;
import smartin.miapi.modules.properties.ComponentProperty;
import smartin.miapi.modules.properties.FakeItemTagProperty;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.onHit.CopyItemOnHit;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GeneratedMaterialPropertyManager {


    public static Map<String, Map<ModuleProperty<?>, Object>> setup(ResourceLocation id, SwordItem swordItem, DiggerItem axeItem, List<Item> toolMaterials, Map<String, Map<ModuleProperty<?>, Object>> old) {
        Map<String, Map<ModuleProperty<?>, Object>> properties = new HashMap<>(old);
        setupProperties(swordItem, id, "blade", Items.DIAMOND_SWORD, Items.WOODEN_SWORD, properties);
        setupProperties(axeItem, id, "axe", Items.DIAMOND_AXE, Items.WOODEN_AXE, properties);
        setupProperties(toolMaterials.stream().filter(PickaxeItem.class::isInstance).findFirst(), id, "pickaxe", Items.DIAMOND_PICKAXE, Items.WOODEN_PICKAXE, properties);
        setupProperties(toolMaterials.stream().filter(ShovelItem.class::isInstance).findFirst(), id, "shovel", Items.DIAMOND_SHOVEL, Items.WOODEN_SHOVEL, properties);
        setupProperties(toolMaterials.stream().filter(HoeItem.class::isInstance).findFirst(), id, "hoe", Items.DIAMOND_HOE, Items.WOODEN_HOE, properties);
        return properties;
    }

    private static void setupProperties(Optional<Item> item, ResourceLocation id, String type, Item vanillaCompare, Item vanillaCompare2, Map<String, Map<ModuleProperty<?>, Object>> properties) {
        item.ifPresent(i -> setupProperties(i, id, type, vanillaCompare, vanillaCompare2, properties));
    }

    private static void setupProperties(Item item, ResourceLocation id, String type, Item vanillaCompare, Item vanillaCompare2, Map<String, Map<ModuleProperty<?>, Object>> properties) {
        // Create an intermediate map to accumulate the properties
        Map<ModuleProperty<?>, Object> propertyMap = new HashMap<>();

        if (shouldApplyProperty(MiapiConfig.INSTANCE.server.generatedMaterials.abilityProperty, id.toString())) {
            propertyMap.put(
                    AbilityMangerProperty.property,
                    Map.of(CopyItemAbility.ability, new CopyItemAbility.ItemContext(item))
            );
        }

        if (shouldApplyProperty(MiapiConfig.INSTANCE.server.generatedMaterials.loreProperty, id.toString())) {
            List<Component> loreAdd = new ArrayList<>();
            item.appendHoverText(item.getDefaultInstance(), Item.TooltipContext.EMPTY, loreAdd, TooltipFlag.ADVANCED);
            propertyMap.put(LoreProperty.property, loreAdd);
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
            var componentTypes = vanillaCompare.components().stream().map(c -> c.type()).collect(Collectors.toSet());
            var commonComponents = vanillaCompare2.components().stream().map(c -> c.type()).filter(c -> componentTypes.contains(c)).collect(Collectors.toSet());

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

    public static <T> JsonElement encode(TypedDataComponent<T> typedDataComponent) {
        DataComponentType<T> componentType = typedDataComponent.type();
        T data = typedDataComponent.value();
        return componentType.codec().encodeStart(JsonOps.INSTANCE, data).getOrThrow();
    }
}
