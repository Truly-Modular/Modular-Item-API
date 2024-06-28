package smartin.miapi.modules.properties;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.architectury.event.EventResult;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
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
    public static final Map<String, Supplier<Attribute>> replaceMap = new HashMap<>();
    public static final Map<Attribute, Float> priorityMap = new HashMap<>();
    public static final List<AttributeTransformer> attributeTransformers = new ArrayList<>();

    public AttributeProperty() {
        property = this;
        ModularItemCache.setSupplier(KEY, (AttributeProperty::createAttributeCache));
        ModularItemCache.setSupplier(KEY + "_unmodifieable", (AttributeProperty::equipmentSlotMultimapMapGenerate));
        priorityMap.put(Attributes.ARMOR.value(), -15.0f);
        priorityMap.put(Attributes.ARMOR_TOUGHNESS.value(), -14.0f);
        priorityMap.put(Attributes.KNOCKBACK_RESISTANCE.value(), -13.0f);
        priorityMap.put(Attributes.ATTACK_DAMAGE.value(), -12.0f);
        priorityMap.put(AttributeRegistry.MAGIC_DAMAGE.value(), -11.5f);
        priorityMap.put(Attributes.ATTACK_SPEED.value(), -11.0f);
        priorityMap.put(AttributeRegistry.CRITICAL_DAMAGE.value(), -10.9f);
        priorityMap.put(AttributeRegistry.CRITICAL_CHANCE.value(), -10.8f);
        priorityMap.put(AttributeRegistry.PROJECTILE_DAMAGE.value(), -10.0f);
        priorityMap.put(AttributeRegistry.PROJECTILE_CRIT_MULTIPLIER.value(), -9.5f);
        priorityMap.put(AttributeRegistry.PROJECTILE_SPEED.value(), -9.0f);
        priorityMap.put(AttributeRegistry.PROJECTILE_ACCURACY.value(), -9.0f);
        priorityMap.put(AttributeRegistry.PROJECTILE_PIERCING.value(), -9.0f);
        priorityMap.put(AttributeRegistry.MINING_SPEED_AXE.value(), -8.0f);
        priorityMap.put(AttributeRegistry.MINING_SPEED_PICKAXE.value(), -8.0f);
        priorityMap.put(AttributeRegistry.MINING_SPEED_HOE.value(), -8.0f);
        priorityMap.put(AttributeRegistry.MINING_SPEED_SHOVEL.value(), -8.0f);
        priorityMap.put(AttributeRegistry.REACH.value(), -7.0f);
        priorityMap.put(AttributeRegistry.ATTACK_RANGE.value(), -7.0f);
        priorityMap.put(AttributeRegistry.BACK_STAB.value(), -6.0f);
        priorityMap.put(AttributeRegistry.SHIELD_BREAK.value(), -6.0f);
        priorityMap.put(AttributeRegistry.ARMOR_CRUSHING.value(), -6.0f);
        MiapiEvents.ITEM_STACK_ATTRIBUTE_EVENT.register((info -> {
            info.attributeModifiers.putAll((AttributeProperty.equipmentSlotMultimapMap(info.itemStack).get(info.equipmentSlot)));
            return EventResult.pass();
        }));
    }

    @Override
    public boolean load(String moduleKey, JsonElement element) throws Exception {
        Multimap<Attribute, EntityAttributeModifierHolder> attributeModifiers = ArrayListMultimap.create();
        for (JsonElement attributeElement : element.getAsJsonArray()) {
            JsonObject attributeJson = attributeElement.getAsJsonObject();
            String attributeName = attributeJson.get("attribute").getAsString();
            double value = StatResolver.resolveDouble(attributeJson.get("value").getAsString(), new ModuleInstance(ItemModule.empty));
            AttributeModifier.Operation operation = getOperation(attributeJson.get("operation").getAsString());
            EquipmentSlot slot = getSlot(attributeJson.get("slot").getAsString());

            Attribute attribute = replaceMap.getOrDefault(attributeName, () -> BuiltInRegistries.ATTRIBUTE.get(ResourceLocation.parse(attributeName))).get();

            ResourceLocation identifier = Miapi.id("modular_attributes");

            if (attributeJson.has("id")) {
                identifier = ResourceLocation.parse(attributeJson.get("id").getAsString());
            }
            AttributeModifier.Operation targetOperation = AttributeModifier.Operation.ADD_VALUE;
            if (attributeJson.has("target_operation")) {
                targetOperation = getOperation(attributeJson.get("target_operation").getAsString());
            }

            if (attribute != null) {
                attributeModifiers.put(attribute, new EntityAttributeModifierHolder(new AttributeModifier(identifier, value, operation), slot, targetOperation));
            }
        }
        return true;
    }

    public static Multimap<Attribute, AttributeModifier> mergeAttributes(Multimap<Attribute, AttributeModifier> old, Multimap<Attribute, AttributeModifier> into) {
        Multimap<Attribute, AttributeModifier> mergedList = LinkedListMultimap.create();
        old.entries().forEach(entityAttributeEntityAttributeModifierHolderEntry -> {
            if (!mergedList.get(entityAttributeEntityAttributeModifierHolderEntry.getKey()).contains(entityAttributeEntityAttributeModifierHolderEntry.getValue())) {
                mergedList.put(entityAttributeEntityAttributeModifierHolderEntry.getKey(), entityAttributeEntityAttributeModifierHolderEntry.getValue());
            }
        });
        into.entries().forEach(entityAttributeEntityAttributeModifierHolderEntry -> {
            if (!mergedList.get(entityAttributeEntityAttributeModifierHolderEntry.getKey()).contains(entityAttributeEntityAttributeModifierHolderEntry.getValue())) {
                mergedList.put(entityAttributeEntityAttributeModifierHolderEntry.getKey(), entityAttributeEntityAttributeModifierHolderEntry.getValue());
            }
        });
        return mergedList;
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

    /**
     * return all attributemodifiers of an itemstack
     *
     * @param itemStack
     * @return
     */
    public static Multimap<Attribute, EntityAttributeModifierHolder> getAttributeModifiers(ItemStack itemStack) {
        Multimap<Attribute, EntityAttributeModifierHolder> map = getAttributeModifiersRaw(itemStack);
        Multimap<Attribute, EntityAttributeModifierHolder> map2 = ArrayListMultimap.create();
        map.entries().forEach((entityAttributeEntityAttributeModifierHolderEntry -> {
            map2.put(entityAttributeEntityAttributeModifierHolderEntry.getKey(), entityAttributeEntityAttributeModifierHolderEntry.getValue());
        }));
        map = map2;
        for (AttributeTransformer transformer : attributeTransformers) {
            Multimap<Attribute, EntityAttributeModifierHolder> map3 = ArrayListMultimap.create();
            transformer.transform(map, itemStack).entries().forEach((entityAttributeEntityAttributeModifierHolderEntry -> {
                map3.put(entityAttributeEntityAttributeModifierHolderEntry.getKey(), entityAttributeEntityAttributeModifierHolderEntry.getValue());
            }));
            map = map3;
        }
        return map;
    }

    /**
     * returns the raw modifiers, shouldnt be used widely
     *
     * @param itemStack
     * @return
     */
    public static Multimap<Attribute, EntityAttributeModifierHolder> getAttributeModifiersRaw(ItemStack itemStack) {
        Multimap<Attribute, EntityAttributeModifierHolder> multimap = ArrayListMultimap.create();
        return ModularItemCache.get(itemStack, KEY, multimap);
    }

    /**
     * Generates the multimap for the Cache
     *
     * @param itemStack
     * @return
     */
    private static Map<EquipmentSlot, Multimap<Attribute, AttributeModifier>> equipmentSlotMultimapMapGenerate(ItemStack itemStack) {
        Map<EquipmentSlot, Multimap<Attribute, AttributeModifier>> map = new HashMap<>();
        Arrays.stream(EquipmentSlot.values()).forEach(equipmentSlot -> {
            map.put(equipmentSlot, Multimaps.unmodifiableMultimap(getAttributeModifiersForSlot(itemStack, equipmentSlot, ArrayListMultimap.create())));
        });
        return map;
    }

    /**
     * returns the Attribute map based on equipmentslot
     * This will be nullsave for all equipmentslot
     *
     * @param itemStack
     * @return
     */
    public static Map<EquipmentSlot, Multimap<Attribute, AttributeModifier>> equipmentSlotMultimapMap(ItemStack itemStack) {
        Map<EquipmentSlot, Multimap<Attribute, AttributeModifier>> replaceMap = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            replaceMap.put(slot, ArrayListMultimap.create());
        }
        return ModularItemCache.get(itemStack, KEY + "_unmodifieable", replaceMap);
    }

    private static Multimap<Attribute, AttributeModifier> getAttributeModifiersForSlot(ItemStack itemStack, EquipmentSlot slot, Multimap<Attribute, AttributeModifier> toAdding) {
        if (itemStack.getItem() instanceof ModularItem) {
            Multimap<Attribute, EntityAttributeModifierHolder> toMerge = AttributeProperty.getAttributeModifiers(itemStack);
            Multimap<Attribute, AttributeModifier> merged = ArrayListMultimap.create();
            Map<ResourceLocation, Multimap<Attribute, AttributeModifier>> mergedAdditive = new HashMap<>();
            Map<ResourceLocation, Multimap<Attribute, AttributeModifier>> mergedMultiBase = new HashMap<>();
            Map<ResourceLocation, Multimap<Attribute, AttributeModifier>> mergedMultiTotal = new HashMap<>();

            // Add existing modifiers from original to merged
            toAdding.entries().forEach(entry -> merged.put(entry.getKey(), entry.getValue()));

            toMerge.forEach((entityAttribute, entityAttributeModifier) -> {
                if (entityAttributeModifier.slot().equals(slot)) {
                    merged.put(entityAttribute, entityAttributeModifier.attributeModifier());
                }
            });

            // Assign merged back to original
            toAdding.clear();
            toAdding.putAll(merged);

            mergedAdditive.forEach((id, entityAttributeEntityAttributeModifierMultimap) -> {
                entityAttributeEntityAttributeModifierMultimap.asMap().forEach((key, collection) -> {
                    double startValue = key.getDefaultValue();
                    double multiply = 1;
                    boolean hasValue = false;
                    for (AttributeModifier entityAttributeModifier : collection) {
                        if (entityAttributeModifier.operation().equals(AttributeModifier.Operation.ADD_VALUE)) {
                            startValue += entityAttributeModifier.amount();
                            hasValue = true;
                        }
                        if (entityAttributeModifier.operation().equals(AttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
                            multiply += entityAttributeModifier.amount();
                        }
                    }
                    startValue = startValue * multiply;
                    for (AttributeModifier entityAttributeModifier : collection) {
                        if (entityAttributeModifier.operation().equals(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
                            startValue = startValue * entityAttributeModifier.amount();
                        }
                    }
                    startValue = startValue - key.getDefaultValue();
                    if ((startValue != 0 || hasValue) && !Double.isNaN(startValue)) {
                        AttributeModifier entityAttributeModifier = new AttributeModifier(id, startValue, AttributeModifier.Operation.ADD_VALUE);
                        toAdding.put(key, entityAttributeModifier);
                    }
                });
            });

            mergedMultiBase.forEach((id, entityAttributeEntityAttributeModifierMultimap) -> {
                entityAttributeEntityAttributeModifierMultimap.asMap().forEach((key, collection) -> {
                    double multiply = 0;
                    for (AttributeModifier entityAttributeModifier : collection) {
                        if (entityAttributeModifier.operation().equals(AttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
                            multiply += entityAttributeModifier.amount();
                        }
                        if (entityAttributeModifier.operation().equals(AttributeModifier.Operation.ADD_VALUE)) {
                            Miapi.LOGGER.warn("Operation Addition(+) is not supported to be merged to Multiply Base(*)");
                        }
                    }
                    for (AttributeModifier entityAttributeModifier : collection) {
                        if (entityAttributeModifier.operation().equals(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
                            multiply = (multiply + 1) * (entityAttributeModifier.amount() + 1) - 1;
                        }
                    }
                    if (!Double.isNaN(multiply) && multiply != 1) {
                        AttributeModifier entityAttributeModifier = new AttributeModifier(id, multiply, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                        toAdding.put(key, entityAttributeModifier);
                    }
                });
            });

            mergedMultiTotal.forEach((id, entityAttributeEntityAttributeModifierMultimap) -> {
                entityAttributeEntityAttributeModifierMultimap.asMap().forEach((key, collection) -> {
                    double multiply = 1;
                    for (AttributeModifier entityAttributeModifier : collection) {
                        if (entityAttributeModifier.operation().equals(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
                            multiply = multiply * entityAttributeModifier.amount();
                        }
                        if (entityAttributeModifier.operation().equals(AttributeModifier.Operation.ADD_VALUE)) {
                            Miapi.LOGGER.warn("Operation Addition(+) is not supported to be merged to Multiply Total(**)");
                        }
                        if (entityAttributeModifier.operation().equals(AttributeModifier.Operation.ADD_VALUE)) {
                            Miapi.LOGGER.warn("Operation Multiply Base(*) is not supported to be merged to Multiply Total(**)");
                        }
                    }
                    if (!Double.isNaN(multiply)) {
                        AttributeModifier entityAttributeModifier = new AttributeModifier(id, multiply, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
                        toAdding.put(key, entityAttributeModifier);
                    }
                });
            });

            return sortMultimap(toAdding);
        }
        return toAdding;
    }

    /**
     * A private function to sort the multimap to provide better view in the gui.
     * Sorting is based on the {@link AttributeProperty#priorityMap}
     *
     * @param multimap
     * @return
     */
    public static Multimap<Attribute, AttributeModifier> sortMultimap(Multimap<Attribute, AttributeModifier> multimap) {
        Comparator<Attribute> comparator = (attribute1, attribute2) -> {
            // Get the priority values for the attributes, using 0 as the default value
            float priority1 = priorityMap.getOrDefault(attribute1, 0f);
            float priority2 = priorityMap.getOrDefault(attribute2, 0f);

            // Sort in ascending order (lower priority values first)
            return Float.compare(priority1, priority2);
        };

        // Sort the keys (attributes) of the Multimap using the comparator
        List<Attribute> sortedKeys = new ArrayList<>(multimap.keySet());
        sortedKeys.sort(comparator);

        // Create a new Multimap with the sorted keys
        Multimap<Attribute, AttributeModifier> sortedMultimap = LinkedListMultimap.create();

        // Iterate over the sorted keys and add the corresponding values to the sorted Multimap
        for (Attribute attribute : sortedKeys) {
            sortedMultimap.putAll(attribute, multimap.get(attribute));
        }

        // Clear the original Multimap and add the sorted entries
        return sortedMultimap;
    }

    /**
     * A util function to make reading the multimap simpler
     *
     * @param rawMap
     * @param slot
     * @param entityAttribute
     * @param fallback
     * @return
     */
    public static double getActualValueFrom(Multimap<Attribute, EntityAttributeModifierHolder> rawMap, EquipmentSlot slot, Attribute entityAttribute, double fallback) {
        Multimap<Attribute, AttributeModifier> map = ArrayListMultimap.create();
        rawMap.forEach(((attribute, entityAttributeModifierHolder) -> {
            if (entityAttributeModifierHolder.slot.equals(slot)) {
                map.put(attribute, entityAttributeModifierHolder.attributeModifier);
            }
        }));
        return getActualValue(map, entityAttribute, fallback);
    }

    /**
     * A Util function to make reading attributes from items easier
     *
     * @param stack
     * @param slot
     * @param entityAttribute
     * @param fallback        if the item does not have this attribute, this value is returned
     * @return the double value of the attribute according to the Itemstack
     */
    public static double getActualValue(ItemStack stack, EquipmentSlot slot, Attribute entityAttribute, double fallback) {
        //TODO: ive got 0 clue what todo with this
        //DataComponentTypes.ATTRIBUTE_MODIFIERS;
        if (entityAttribute == null) {
            return fallback;
        }
        List<AttributeModifier> modifiers = new ArrayList<>();
        stack.get(DataComponents.ATTRIBUTE_MODIFIERS).forEach(slot, (attribute, modifier) -> {
            if (entityAttribute.equals(attribute)) {
                modifiers.add(modifier);
            }
        });
        return getActualValue(modifiers, fallback);
    }

    public static boolean hasAttribute(Multimap<Attribute, AttributeModifier> map, Attribute entityAttribute, double fallback) {
        Collection<AttributeModifier> attributes = map.get(entityAttribute);
        return !attributes.isEmpty();
    }

    public static double getActualValue(Multimap<Attribute, AttributeModifier> map, Attribute entityAttribute) {
        return getActualValue(map, entityAttribute, entityAttribute.getDefaultValue());
    }

    public static double getActualValue(Multimap<Attribute, AttributeModifier> map, Attribute entityAttribute, double fallback) {
        Collection<AttributeModifier> attributes = map.get(entityAttribute);
        return getActualValue(attributes, fallback);
    }

    public static double getActualValue(Collection<AttributeModifier> attributes, double fallback) {
        List<Double> addition = new ArrayList<>();
        List<Double> multiplyBase = new ArrayList<>();
        List<Double> multiplyTotal = new ArrayList<>();
        attributes.forEach(attribute -> {
            switch (attribute.operation()) {
                case ADD_VALUE -> addition.add(attribute.amount());
                case ADD_MULTIPLIED_BASE -> multiplyBase.add(attribute.amount());
                case ADD_MULTIPLIED_TOTAL -> multiplyTotal.add(attribute.amount());
            }
        });
        double value = fallback;
        for (Double currentValue : addition) {
            value += currentValue;
        }
        double multiplier = 1.0;
        for (Double currentValue : multiplyBase) {
            multiplier += currentValue;
        }
        value = value * multiplier;
        for (Double currentValue : multiplyTotal) {
            value = (1 + currentValue) * value;
        }
        if (Double.isNaN(value)) {
            return fallback;
        }
        return value;
    }

    /**
     * A Util function to make reading attributes from items easier
     *
     * @param stack
     * @param slot
     * @param entityAttribute
     * @return the double value of the attribute according to the Itemstack
     */
    public static double getActualValue(ItemStack stack, EquipmentSlot slot, Attribute entityAttribute) {
        return getActualValue(stack, slot, entityAttribute, entityAttribute.getDefaultValue());
    }

    private static Multimap<Attribute, EntityAttributeModifierHolder> createAttributeCache(ItemStack itemStack) {
        return createAttributeMap(itemStack, AttributeProperty::getIDforSlot);
    }


    public static Multimap<Attribute, EntityAttributeModifierHolder> createAttributeMap(ItemStack itemStack, IdentifierGetter defaultID) {
        ModuleInstance rootInstance = ItemModule.getModules(itemStack);
        Multimap<Attribute, EntityAttributeModifierHolder> attributeModifiers = ArrayListMultimap.create();
        for (ModuleInstance instance : rootInstance.allSubModules()) {
            getAttributeModifiers(defaultID, instance, attributeModifiers);
        }
        return attributeModifiers;
    }

    public static void getAttributeModifiers(IdentifierGetter defaultID, ModuleInstance instance, Multimap<Attribute, EntityAttributeModifierHolder> attributeModifiers) {
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
            AttributeModifier.Operation operation = getOperation(attributeJson.operation);
            AttributeModifier.Operation baseTarget = getOperation(attributeJson.target_operation);
            Attribute attribute = replaceMap.getOrDefault(attributeName, () -> BuiltInRegistries.ATTRIBUTE.get(ResourceLocation.parse(attributeName))).get();
            if (attribute == null) {
                Miapi.LOGGER.warn(String.valueOf(BuiltInRegistries.ATTRIBUTE.get(ResourceLocation.parse(attributeName))));
                Miapi.LOGGER.warn("Attribute is null " + attributeName + " on module " + instance.module.name() + " this should not have happened.");
            } else {
                ResourceLocation identifier = getIDforSlot(slot, attribute, operation);
                //TODO:verify i dont need todo this anymoey
                /*
                if (identifier.equals(ExampleModularItem.attackDamageUUID())) {
                    identifier = ExampleModularItem.attackSpeedUUID();
                }
                if (identifier.equals(ExampleModularItem.attackSpeedUUID())) {
                    identifier = ExampleModularItem.attackSpeedUUID();
                }

                 */
                attributeModifiers.put(attribute, new EntityAttributeModifierHolder(new AttributeModifier(identifier, value, operation), slot, baseTarget));
            }
        }
    }

    /**
     * Generates a unique id for the slot to prevent collisions
     *
     * @param equipmentSlot
     * @return a unique ID for the slot
     */
    public static ResourceLocation getIDforSlot(EquipmentSlot equipmentSlot, Attribute attribute, AttributeModifier.Operation operation) {
        return getIDforSlot(equipmentSlot, attribute, operation, "");
    }

    /**
     * Generates a unique id for the slot to prevent collisions
     *
     * @param equipmentSlot
     * @return a unique ID for the slot
     */
    public static ResourceLocation getIDforSlot(EquipmentSlot equipmentSlot, Attribute attribute, AttributeModifier.Operation operation, String context) {
        String slotidString = equipmentSlot.getName() + "-" + attribute.getDescriptionId() + "-" + equipmentSlot.getIndex() + "-" + equipmentSlot.getFilterFlag() + "-" + operation.toString() + context;
        return getIDforSlot(slotidString);
    }

    public static ResourceLocation getIDforSlot(String slotidString) {
        return Miapi.id(slotidString);
    }

    private static AttributeModifier.Operation getOperation(String operationString) {
        if (operationString == null) {
            return AttributeModifier.Operation.ADD_VALUE;
        }
        return switch (operationString) {
            case "*" -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
            case "**" -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
            default -> AttributeModifier.Operation.ADD_VALUE;
        };
    }

    public static EquipmentSlot getSlot(String slotString) {
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

    public record EntityAttributeModifierHolder(AttributeModifier attributeModifier, EquipmentSlot slot,
                                                AttributeModifier.Operation mergeTo) {
    }

    public interface AttributeTransformer {
        Multimap<Attribute, EntityAttributeModifierHolder> transform(Multimap<Attribute, EntityAttributeModifierHolder> map, ItemStack itemstack);
    }

    public interface IdentifierGetter {
        ResourceLocation fromSlot(EquipmentSlot equipmentSlot, Attribute attribute, AttributeModifier.Operation operation);
    }

    public static class AttributeJson {
        public String attribute;
        public String value;
        public String operation;
        public String slot;
        public String id;
        public String target_operation;
    }
}
