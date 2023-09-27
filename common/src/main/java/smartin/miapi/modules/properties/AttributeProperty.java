package smartin.miapi.modules.properties;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.*;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.item.modular.items.ExampleModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.*;
import java.util.function.Supplier;

/**
 * This property allows Modules to set Attributes
 */
public class AttributeProperty implements ModuleProperty {
    public static final String KEY = "attributes";
    public static ModuleProperty property;
    public static final Map<String, Supplier<EntityAttribute>> replaceMap = new HashMap<>();
    public static final Map<EntityAttribute, Float> priorityMap = new HashMap<>();
    public static final List<AttributeTransformer> attributeTransformers = new ArrayList<>();

    public AttributeProperty() {
        property = this;
        ModularItemCache.setSupplier(KEY, (AttributeProperty::createAttributeCache));
        priorityMap.put(EntityAttributes.GENERIC_ARMOR, -15.0f);
        priorityMap.put(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, -14.0f);
        priorityMap.put(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, -13.0f);
        priorityMap.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, -12.0f);
        priorityMap.put(EntityAttributes.GENERIC_ATTACK_SPEED, -11.0f);
        priorityMap.put(AttributeRegistry.PROJECTILE_DAMAGE, -10.0f);
        priorityMap.put(AttributeRegistry.PROJECTILE_CRIT_MULTIPLIER, -10.0f);
        priorityMap.put(AttributeRegistry.PROJECTILE_SPEED, -9.0f);
        priorityMap.put(AttributeRegistry.PROJECTILE_ACCURACY, -9.0f);
        priorityMap.put(AttributeRegistry.PROJECTILE_PIERCING, -9.0f);
        priorityMap.put(AttributeRegistry.MINING_SPEED_AXE, -8.0f);
        priorityMap.put(AttributeRegistry.MINING_SPEED_PICKAXE, -8.0f);
        priorityMap.put(AttributeRegistry.MINING_SPEED_HOE, -8.0f);
        priorityMap.put(AttributeRegistry.MINING_SPEED_SHOVEL, -8.0f);
        priorityMap.put(AttributeRegistry.REACH, -7.0f);
        priorityMap.put(AttributeRegistry.ATTACK_RANGE, -7.0f);
        priorityMap.put(AttributeRegistry.BACK_STAB, -6.0f);
        priorityMap.put(AttributeRegistry.SHIELD_BREAK, -6.0f);
        priorityMap.put(AttributeRegistry.ARMOR_CRUSHING, -6.0f);
    }

    @Override
    public boolean load(String moduleKey, JsonElement element) throws Exception {
        Multimap<EntityAttribute, EntityAttributeModifierHolder> attributeModifiers = ArrayListMultimap.create();
        for (JsonElement attributeElement : element.getAsJsonArray()) {
            JsonObject attributeJson = attributeElement.getAsJsonObject();
            String attributeName = attributeJson.get("attribute").getAsString();
            double value = StatResolver.resolveDouble(attributeJson.get("value").getAsString(), new ItemModule.ModuleInstance(ItemModule.empty));
            EntityAttributeModifier.Operation operation = getOperation(attributeJson.get("operation").getAsString());
            EquipmentSlot slot = getSlot(attributeJson.get("slot").getAsString());

            EntityAttribute attribute = replaceMap.getOrDefault(attributeName, () -> Registries.ATTRIBUTE.get(new Identifier(attributeName))).get();

            UUID uuid = null;

            if (attributeJson.has("uuid")) {
                uuid = UUID.fromString(attributeJson.get("uuid").getAsString());
            }
            String modifierName = attributeName;
            if (attributeJson.has("name")) {
                modifierName = attributeJson.get("name").getAsString();
            }

            if (attribute != null) {
                if (uuid != null) {
                    // Use constructor with UUID
                    attributeModifiers.put(attribute, new EntityAttributeModifierHolder(new EntityAttributeModifier(uuid, modifierName, value, operation), slot, true));
                } else {
                    // Use constructor without UUID
                    attributeModifiers.put(attribute, new EntityAttributeModifierHolder(new EntityAttributeModifier(modifierName, value, operation), slot, true));
                }
            }
        }
        return true;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        switch (type) {
            case SMART, EXTEND -> {
                JsonElement element = old.deepCopy();
                element.getAsJsonArray().addAll(toMerge.getAsJsonArray());
                return element;
            }
            case OVERWRITE -> {
                return toMerge;
            }
        }
        return old;
    }

    public static Multimap<EntityAttribute, EntityAttributeModifierHolder> getAttributeModifiers(ItemStack itemStack) {
        Multimap<EntityAttribute, EntityAttributeModifierHolder> map = getAttributeModifiersRaw(itemStack);
        Multimap<EntityAttribute, EntityAttributeModifierHolder> map2 = ArrayListMultimap.create();
        map.entries().forEach((entityAttributeEntityAttributeModifierHolderEntry -> {
            map2.put(entityAttributeEntityAttributeModifierHolderEntry.getKey(), entityAttributeEntityAttributeModifierHolderEntry.getValue());
        }));
        map = map2;
        for (AttributeTransformer transformer : attributeTransformers) {
            Multimap<EntityAttribute, EntityAttributeModifierHolder> map3 = ArrayListMultimap.create();
            transformer.transform(map, itemStack).entries().forEach((entityAttributeEntityAttributeModifierHolderEntry -> {
                map3.put(entityAttributeEntityAttributeModifierHolderEntry.getKey(), entityAttributeEntityAttributeModifierHolderEntry.getValue());
            }));
            map = map3;
        }
        return map;
    }

    public static Multimap<EntityAttribute, EntityAttributeModifierHolder> getAttributeModifiersRaw(ItemStack itemStack) {
        return (Multimap<EntityAttribute, EntityAttributeModifierHolder>) ModularItemCache.get(itemStack, KEY);
    }

    public static Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiersForSlot(ItemStack itemStack, EquipmentSlot slot, Multimap<EntityAttribute, EntityAttributeModifier> toAdding) {
        if (itemStack.getItem() instanceof ModularItem) {
            Multimap<EntityAttribute, AttributeProperty.EntityAttributeModifierHolder> toMerge = AttributeProperty.getAttributeModifiers(itemStack);
            Multimap<EntityAttribute, EntityAttributeModifier> merged = ArrayListMultimap.create();
            Multimap<EntityAttribute, EntityAttributeModifier> mergedOnItem = ArrayListMultimap.create();

            // Add existing modifiers from original to merged
            toAdding.entries().forEach(entry -> merged.put(entry.getKey(), entry.getValue()));

            toMerge.forEach((entityAttribute, entityAttributeModifier) -> {
                if (entityAttributeModifier.slot().equals(slot)) {
                    if (entityAttributeModifier.seperateOnItem) {
                        merged.put(entityAttribute, entityAttributeModifier.attributeModifier());
                    } else {
                        mergedOnItem.put(entityAttribute, entityAttributeModifier.attributeModifier());
                    }
                }
            });

            // Assign merged back to original
            toAdding.clear();
            toAdding.putAll(merged);
            Map<UUID, Multimap<EntityAttribute, EntityAttributeModifier>> uuidMultimapMap = new HashMap<>();

            mergedOnItem.forEach((attribute, attributeModifier) -> {
                Multimap<EntityAttribute, EntityAttributeModifier> multimap = uuidMultimapMap.computeIfAbsent(attributeModifier.getId(), (id) -> ArrayListMultimap.create());
                if (attribute == null) {
                    Miapi.LOGGER.warn("Attribute is null?! - this should never happen");
                } else {
                    multimap.put(attribute, attributeModifier);
                }
            });

            uuidMultimapMap.forEach((uuid, entityAttributeEntityAttributeModifierMultimap) -> {
                entityAttributeEntityAttributeModifierMultimap.asMap().forEach((key, collection) -> {
                    double startValue = key.getDefaultValue();
                    double multiply = 1;
                    for (EntityAttributeModifier entityAttributeModifier : collection) {
                        if (entityAttributeModifier.getOperation().equals(EntityAttributeModifier.Operation.ADDITION)) {
                            startValue += entityAttributeModifier.getValue();
                        }
                        if (entityAttributeModifier.getOperation().equals(EntityAttributeModifier.Operation.MULTIPLY_BASE)) {
                            multiply += entityAttributeModifier.getValue();
                        }
                    }
                    startValue = startValue * multiply;
                    for (EntityAttributeModifier entityAttributeModifier : collection) {
                        if (entityAttributeModifier.getOperation().equals(EntityAttributeModifier.Operation.MULTIPLY_TOTAL)) {
                            startValue = startValue * entityAttributeModifier.getValue();
                        }
                    }
                    startValue = startValue - key.getDefaultValue();
                    if (startValue != 0) {
                        EntityAttributeModifier entityAttributeModifier = new EntityAttributeModifier(uuid, "generic.miapi." + key.getTranslationKey(), startValue, EntityAttributeModifier.Operation.ADDITION);
                        toAdding.put(key, entityAttributeModifier);
                    }
                });
            });

            return sortMultimap(toAdding);
        }
        return toAdding;
    }

    private static Multimap<EntityAttribute, EntityAttributeModifier> sortMultimap(Multimap<EntityAttribute, EntityAttributeModifier> multimap) {
        Comparator<EntityAttribute> comparator = (attribute1, attribute2) -> {
            // Get the priority values for the attributes, using 0 as the default value
            float priority1 = priorityMap.getOrDefault(attribute1, 0f);
            float priority2 = priorityMap.getOrDefault(attribute2, 0f);

            // Sort in ascending order (lower priority values first)
            return Float.compare(priority1, priority2);
        };

        // Sort the keys (attributes) of the Multimap using the comparator
        List<EntityAttribute> sortedKeys = new ArrayList<>(multimap.keySet());
        sortedKeys.sort(comparator);

        // Create a new Multimap with the sorted keys
        Multimap<EntityAttribute, EntityAttributeModifier> sortedMultimap = LinkedListMultimap.create();

        // Iterate over the sorted keys and add the corresponding values to the sorted Multimap
        for (EntityAttribute attribute : sortedKeys) {
            sortedMultimap.putAll(attribute, multimap.get(attribute));
        }

        // Clear the original Multimap and add the sorted entries
        return sortedMultimap;
    }

    public static double getActualValueFrom(Multimap<EntityAttribute, EntityAttributeModifierHolder> rawMap, EquipmentSlot slot, EntityAttribute entityAttribute, double fallback) {

        DefaultAttributeContainer container = DefaultAttributeContainer.builder().add(entityAttribute).build();

        AttributeContainer container1 = new AttributeContainer(container);

        Multimap<EntityAttribute, EntityAttributeModifier> map = ArrayListMultimap.create();
        rawMap.forEach(((attribute, entityAttributeModifierHolder) -> {
            if (entityAttributeModifierHolder.slot.equals(slot)) {
                map.put(attribute, entityAttributeModifierHolder.attributeModifier);
            }
        }));

        container1.addTemporaryModifiers(map);
        if (container1.hasAttribute(entityAttribute)) {
            return container1.getValue(entityAttribute);
        } else {
            return fallback;
        }
    }

    public static double getActualValue(ItemStack stack, EquipmentSlot slot, EntityAttribute entityAttribute, double fallback) {
        Collection<EntityAttributeModifier> attributes = stack.getAttributeModifiers(slot).get(entityAttribute);
        Multimap<EntityAttribute, EntityAttributeModifier> map = HashMultimap.create();
        attributes.forEach(attribute -> {
            map.put(entityAttribute, attribute);
        });

        DefaultAttributeContainer container = DefaultAttributeContainer.builder().add(entityAttribute).build();

        AttributeContainer container1 = new AttributeContainer(container);

        container1.addTemporaryModifiers(map);
        if (map.containsKey(entityAttribute) && container1.hasAttribute(entityAttribute)) {
            return container1.getValue(entityAttribute);
        } else {
            return fallback;
        }
    }

    public static double getActualValue(ItemStack stack, EquipmentSlot slot, EntityAttribute entityAttribute) {
        return getActualValue(stack, slot, entityAttribute, entityAttribute.getDefaultValue());
    }

    private static Multimap<EntityAttribute, EntityAttributeModifierHolder> createAttributeCache(ItemStack itemStack) {
        ItemModule.ModuleInstance rootInstance = ItemModule.getModules(itemStack);
        Multimap<EntityAttribute, EntityAttributeModifierHolder> attributeModifiers = ArrayListMultimap.create();
        for (ItemModule.ModuleInstance instance : rootInstance.allSubModules()) {
            getAttributeModifiers(instance, attributeModifiers);
        }
        return attributeModifiers;
    }

    public static void getAttributeModifiers(ItemModule.ModuleInstance instance, Multimap<EntityAttribute, EntityAttributeModifierHolder> attributeModifiers) {
        JsonElement element = instance.getProperties().get(property);
        if (element == null) {
            return;
        }
        for (JsonElement attributeElement : element.getAsJsonArray()) {
            AttributeJson attributeJson = Miapi.gson.fromJson(attributeElement, AttributeJson.class);
            assert attributeJson.attribute != null;
            assert attributeJson.value != null;
            assert attributeJson.operation != null;
            EquipmentSlot slot = (attributeJson.slot != null) ? getSlot(attributeJson.slot) : EquipmentSlot.MAINHAND;
            String attributeName = attributeJson.attribute;
            double value = StatResolver.resolveDouble(attributeJson.value, instance);
            EntityAttributeModifier.Operation operation = getOperation(attributeJson.operation);
            EntityAttribute attribute = replaceMap.getOrDefault(attributeName, () -> Registries.ATTRIBUTE.get(new Identifier(attributeName))).get();
            if (attribute == null) {
                Miapi.LOGGER.warn(String.valueOf(Registries.ATTRIBUTE.get(new Identifier(attributeName))));
                Miapi.LOGGER.warn("Attribute is null " + attributeName + " on module " + instance.module.getName() + " this should not have happened.");
            } else {
                if (attributeJson.uuid != null) {
                    UUID uuid = UUID.fromString(attributeJson.uuid);
                    // Thanks Mojang for using == and not .equals so i have to do this abomination
                    if (uuid.equals(ExampleModularItem.attackDamageUUID())) {
                        uuid = ExampleModularItem.attackDamageUUID();
                    }
                    if (uuid.equals(ExampleModularItem.attackSpeedUUID())) {
                        uuid = ExampleModularItem.attackSpeedUUID();
                    }
                    attributeModifiers.put(attribute, new EntityAttributeModifierHolder(new EntityAttributeModifier(uuid, attributeName, value, operation), slot, attributeJson.seperateOnItem));
                } else {
                    // Use constructor without UUID
                    attributeModifiers.put(attribute, new EntityAttributeModifierHolder(new EntityAttributeModifier(attributeName, value, operation), slot, attributeJson.seperateOnItem));
                }
            }
        }
    }

    private static EntityAttributeModifier.Operation getOperation(String operationString) {
        return switch (operationString) {
            case "*" -> EntityAttributeModifier.Operation.MULTIPLY_BASE;
            case "**" -> EntityAttributeModifier.Operation.MULTIPLY_TOTAL;
            default -> EntityAttributeModifier.Operation.ADDITION;
        };
    }

    private static EquipmentSlot getSlot(String slotString) {
        if (slotString != null && !slotString.isEmpty()) {
            try {
                return EquipmentSlot.byName(slotString);
            } catch (Exception e) {
                Miapi.LOGGER.error("Equipment Slot not found - use correct spelling please [mainhand offhand feet legs chest head]");
                Miapi.LOGGER.error("substituting mainhand Slot Instead");
                e.printStackTrace();
            }
        }
        return EquipmentSlot.MAINHAND; // default to main hand if slot is not specified
    }

    public record EntityAttributeModifierHolder(EntityAttributeModifier attributeModifier, EquipmentSlot slot,
                                                boolean seperateOnItem) {
    }

    public interface AttributeTransformer {
        Multimap<EntityAttribute, EntityAttributeModifierHolder> transform(Multimap<EntityAttribute, EntityAttributeModifierHolder> map, ItemStack itemstack);
    }

    public static class AttributeJson {
        public String attribute;
        public String value;
        public String operation;
        public String slot;
        public String uuid;
        public boolean seperateOnItem;
    }
}
