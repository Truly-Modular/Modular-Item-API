package smartin.miapi.modules.properties;

import com.google.common.collect.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.architectury.event.EventResult;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.*;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
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
    public static final Map<String, Supplier<EntityAttribute>> replaceMap = new HashMap<>();
    public static final Map<EntityAttribute, Float> priorityMap = new HashMap<>();
    public static final List<AttributeTransformer> attributeTransformers = new ArrayList<>();

    public AttributeProperty() {
        property = this;
        ModularItemCache.setSupplier(KEY, (AttributeProperty::createAttributeCache));
        ModularItemCache.setSupplier(KEY + "_unmodifieable", (AttributeProperty::equipmentSlotMultimapMapGenerate));
        priorityMap.put(EntityAttributes.GENERIC_ARMOR.comp_349(), -15.0f);
        priorityMap.put(EntityAttributes.GENERIC_ARMOR_TOUGHNESS.comp_349(), -14.0f);
        priorityMap.put(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE.comp_349(), -13.0f);
        priorityMap.put(EntityAttributes.GENERIC_ATTACK_DAMAGE.comp_349(), -12.0f);
        priorityMap.put(AttributeRegistry.MAGIC_DAMAGE, -11.5f);
        priorityMap.put(EntityAttributes.GENERIC_ATTACK_SPEED.comp_349(), -11.0f);
        priorityMap.put(AttributeRegistry.CRITICAL_DAMAGE, -10.9f);
        priorityMap.put(AttributeRegistry.CRITICAL_CHANCE, -10.8f);
        priorityMap.put(AttributeRegistry.PROJECTILE_DAMAGE, -10.0f);
        priorityMap.put(AttributeRegistry.PROJECTILE_CRIT_MULTIPLIER, -9.5f);
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
        MiapiEvents.ITEM_STACK_ATTRIBUTE_EVENT.register((info -> {
            info.attributeModifiers.putAll((AttributeProperty.equipmentSlotMultimapMap(info.itemStack).get(info.equipmentSlot)));
            return EventResult.pass();
        }));
    }

    @Override
    public boolean load(String moduleKey, JsonElement element) throws Exception {
        Multimap<EntityAttribute, EntityAttributeModifierHolder> attributeModifiers = ArrayListMultimap.create();
        for (JsonElement attributeElement : element.getAsJsonArray()) {
            JsonObject attributeJson = attributeElement.getAsJsonObject();
            String attributeName = attributeJson.get("attribute").getAsString();
            double value = StatResolver.resolveDouble(attributeJson.get("value").getAsString(), new ModuleInstance(ItemModule.empty));
            EntityAttributeModifier.Operation operation = getOperation(attributeJson.get("operation").getAsString());
            EquipmentSlot slot = getSlot(attributeJson.get("slot").getAsString());

            EntityAttribute attribute = replaceMap.getOrDefault(attributeName, () -> Registries.ATTRIBUTE.get(Identifier.of(attributeName))).get();

            Identifier identifier = Miapi.MiapiIdentifier("modular_attributes");

            if (attributeJson.has("id")) {
                identifier = Identifier.of(attributeJson.get("id").getAsString());
            }
            EntityAttributeModifier.Operation targetOperation = EntityAttributeModifier.Operation.ADD_VALUE;
            if (attributeJson.has("target_operation")) {
                targetOperation = getOperation(attributeJson.get("target_operation").getAsString());
            }

            if (attribute != null) {
                attributeModifiers.put(attribute, new EntityAttributeModifierHolder(new EntityAttributeModifier(identifier, value, operation), slot, targetOperation));
            }
        }
        return true;
    }

    public static Multimap<EntityAttribute, EntityAttributeModifier> mergeAttributes(Multimap<EntityAttribute, EntityAttributeModifier> old, Multimap<EntityAttribute, EntityAttributeModifier> into) {
        Multimap<EntityAttribute, EntityAttributeModifier> mergedList = LinkedListMultimap.create();
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

    /**
     * returns the raw modifiers, shouldnt be used widely
     *
     * @param itemStack
     * @return
     */
    public static Multimap<EntityAttribute, EntityAttributeModifierHolder> getAttributeModifiersRaw(ItemStack itemStack) {
        Multimap<EntityAttribute, EntityAttributeModifierHolder> multimap = ArrayListMultimap.create();
        return ModularItemCache.get(itemStack, KEY, multimap);
    }

    /**
     * Generates the multimap for the Cache
     *
     * @param itemStack
     * @return
     */
    private static Map<EquipmentSlot, Multimap<EntityAttribute, EntityAttributeModifier>> equipmentSlotMultimapMapGenerate(ItemStack itemStack) {
        Map<EquipmentSlot, Multimap<EntityAttribute, EntityAttributeModifier>> map = new HashMap<>();
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
    public static Map<EquipmentSlot, Multimap<EntityAttribute, EntityAttributeModifier>> equipmentSlotMultimapMap(ItemStack itemStack) {
        Map<EquipmentSlot, Multimap<EntityAttribute, EntityAttributeModifier>> replaceMap = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            replaceMap.put(slot, ArrayListMultimap.create());
        }
        return ModularItemCache.get(itemStack, KEY + "_unmodifieable", replaceMap);
    }

    private static Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiersForSlot(ItemStack itemStack, EquipmentSlot slot, Multimap<EntityAttribute, EntityAttributeModifier> toAdding) {
        if (itemStack.getItem() instanceof ModularItem) {
            Multimap<EntityAttribute, AttributeProperty.EntityAttributeModifierHolder> toMerge = AttributeProperty.getAttributeModifiers(itemStack);
            Multimap<EntityAttribute, EntityAttributeModifier> merged = ArrayListMultimap.create();
            Map<Identifier, Multimap<EntityAttribute, EntityAttributeModifier>> mergedAdditive = new HashMap<>();
            Map<Identifier, Multimap<EntityAttribute, EntityAttributeModifier>> mergedMultiBase = new HashMap<>();
            Map<Identifier, Multimap<EntityAttribute, EntityAttributeModifier>> mergedMultiTotal = new HashMap<>();

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
                    for (EntityAttributeModifier entityAttributeModifier : collection) {
                        if (entityAttributeModifier.comp_2450().equals(EntityAttributeModifier.Operation.ADD_VALUE)) {
                            startValue += entityAttributeModifier.value();
                            hasValue = true;
                        }
                        if (entityAttributeModifier.comp_2450().equals(EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
                            multiply += entityAttributeModifier.value();
                        }
                    }
                    startValue = startValue * multiply;
                    for (EntityAttributeModifier entityAttributeModifier : collection) {
                        if (entityAttributeModifier.comp_2450().equals(EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
                            startValue = startValue * entityAttributeModifier.value();
                        }
                    }
                    startValue = startValue - key.getDefaultValue();
                    if ((startValue != 0 || hasValue) && !Double.isNaN(startValue)) {
                        EntityAttributeModifier entityAttributeModifier = new EntityAttributeModifier(id, startValue, EntityAttributeModifier.Operation.ADD_VALUE);
                        toAdding.put(key, entityAttributeModifier);
                    }
                });
            });

            mergedMultiBase.forEach((id, entityAttributeEntityAttributeModifierMultimap) -> {
                entityAttributeEntityAttributeModifierMultimap.asMap().forEach((key, collection) -> {
                    double multiply = 0;
                    for (EntityAttributeModifier entityAttributeModifier : collection) {
                        if (entityAttributeModifier.comp_2450().equals(EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
                            multiply += entityAttributeModifier.value();
                        }
                        if (entityAttributeModifier.comp_2450().equals(EntityAttributeModifier.Operation.ADD_VALUE)) {
                            Miapi.LOGGER.warn("Operation Addition(+) is not supported to be merged to Multiply Base(*)");
                        }
                    }
                    for (EntityAttributeModifier entityAttributeModifier : collection) {
                        if (entityAttributeModifier.comp_2450().equals(EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
                            multiply = (multiply + 1) * (entityAttributeModifier.value() + 1) - 1;
                        }
                    }
                    if (!Double.isNaN(multiply) && multiply != 1) {
                        EntityAttributeModifier entityAttributeModifier = new EntityAttributeModifier(id, multiply, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                        toAdding.put(key, entityAttributeModifier);
                    }
                });
            });

            mergedMultiTotal.forEach((id, entityAttributeEntityAttributeModifierMultimap) -> {
                entityAttributeEntityAttributeModifierMultimap.asMap().forEach((key, collection) -> {
                    double multiply = 1;
                    for (EntityAttributeModifier entityAttributeModifier : collection) {
                        if (entityAttributeModifier.comp_2450().equals(EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
                            multiply = multiply * entityAttributeModifier.value();
                        }
                        if (entityAttributeModifier.comp_2450().equals(EntityAttributeModifier.Operation.ADD_VALUE)) {
                            Miapi.LOGGER.warn("Operation Addition(+) is not supported to be merged to Multiply Total(**)");
                        }
                        if (entityAttributeModifier.comp_2450().equals(EntityAttributeModifier.Operation.ADD_VALUE)) {
                            Miapi.LOGGER.warn("Operation Multiply Base(*) is not supported to be merged to Multiply Total(**)");
                        }
                    }
                    if (!Double.isNaN(multiply)) {
                        EntityAttributeModifier entityAttributeModifier = new EntityAttributeModifier(id, multiply, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
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
    public static Multimap<EntityAttribute, EntityAttributeModifier> sortMultimap(Multimap<EntityAttribute, EntityAttributeModifier> multimap) {
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

    /**
     * A util function to make reading the multimap simpler
     *
     * @param rawMap
     * @param slot
     * @param entityAttribute
     * @param fallback
     * @return
     */
    public static double getActualValueFrom(Multimap<EntityAttribute, EntityAttributeModifierHolder> rawMap, EquipmentSlot slot, EntityAttribute entityAttribute, double fallback) {
        Multimap<EntityAttribute, EntityAttributeModifier> map = ArrayListMultimap.create();
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
    public static double getActualValue(ItemStack stack, EquipmentSlot slot, EntityAttribute entityAttribute, double fallback) {
        //TODO: ive got 0 clue what todo with this
        //DataComponentTypes.ATTRIBUTE_MODIFIERS;
        if (entityAttribute == null) {
            return fallback;
        }
        List<EntityAttributeModifier> modifiers = new ArrayList<>();
        stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS).applyModifiers(slot, (attribute, modifier) -> {
            if (entityAttribute.equals(attribute)) {
                modifiers.add(modifier);
            }
        });
        return getActualValue(modifiers, fallback);
    }

    public static boolean hasAttribute(Multimap<EntityAttribute, EntityAttributeModifier> map, EntityAttribute entityAttribute, double fallback) {
        Collection<EntityAttributeModifier> attributes = map.get(entityAttribute);
        return !attributes.isEmpty();
    }

    public static double getActualValue(Multimap<EntityAttribute, EntityAttributeModifier> map, EntityAttribute entityAttribute, double fallback) {
        Collection<EntityAttributeModifier> attributes = map.get(entityAttribute);
        return getActualValue(attributes, fallback);
        /*
        DefaultAttributeContainer container = DefaultAttributeContainer.builder().add(entityAttribute).build();

        AttributeContainer container1 = new AttributeContainer(container);

        container1.addTemporaryModifiers(map);
        if (map.containsKey(entityAttribute) && container1.hasAttribute(entityAttribute)) {
            return container1.getInt(entityAttribute);
        } else {
            return fallback;
        }
        */
    }

    public static double getActualValue(Collection<EntityAttributeModifier> attributes, double fallback) {
        List<Double> addition = new ArrayList<>();
        List<Double> multiplyBase = new ArrayList<>();
        List<Double> multiplyTotal = new ArrayList<>();
        attributes.forEach(attribute -> {
            switch (attribute.comp_2450()) {
                case ADD_VALUE -> addition.add(attribute.value());
                case ADD_MULTIPLIED_BASE -> multiplyBase.add(attribute.value());
                case ADD_MULTIPLIED_TOTAL -> multiplyTotal.add(attribute.value());
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
    public static double getActualValue(ItemStack stack, EquipmentSlot slot, EntityAttribute entityAttribute) {
        return getActualValue(stack, slot, entityAttribute, entityAttribute.getDefaultValue());
    }

    private static Multimap<EntityAttribute, EntityAttributeModifierHolder> createAttributeCache(ItemStack itemStack) {
        return createAttributeMap(itemStack, AttributeProperty::getIDforSlot);
    }


    public static Multimap<EntityAttribute, EntityAttributeModifierHolder> createAttributeMap(ItemStack itemStack, IdentifierGetter defaultID) {
        ModuleInstance rootInstance = ItemModule.getModules(itemStack);
        Multimap<EntityAttribute, EntityAttributeModifierHolder> attributeModifiers = ArrayListMultimap.create();
        for (ModuleInstance instance : rootInstance.allSubModules()) {
            getAttributeModifiers(defaultID, instance, attributeModifiers);
        }
        return attributeModifiers;
    }

    public static void getAttributeModifiers(IdentifierGetter defaultID, ModuleInstance instance, Multimap<EntityAttribute, EntityAttributeModifierHolder> attributeModifiers) {
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
            EntityAttributeModifier.Operation baseTarget = getOperation(attributeJson.target_operation);
            EntityAttribute attribute = replaceMap.getOrDefault(attributeName, () -> Registries.ATTRIBUTE.get(Identifier.of(attributeName))).get();
            if (attribute == null) {
                Miapi.LOGGER.warn(String.valueOf(Registries.ATTRIBUTE.get(Identifier.of(attributeName))));
                Miapi.LOGGER.warn("Attribute is null " + attributeName + " on module " + instance.module.name() + " this should not have happened.");
            } else {
                Identifier identifier = getIDforSlot(slot, attribute, operation);
                //TODO:verify i dont need todo this anymoey
                /*
                if (identifier.equals(ExampleModularItem.attackDamageUUID())) {
                    identifier = ExampleModularItem.attackSpeedUUID();
                }
                if (identifier.equals(ExampleModularItem.attackSpeedUUID())) {
                    identifier = ExampleModularItem.attackSpeedUUID();
                }

                 */
                attributeModifiers.put(attribute, new EntityAttributeModifierHolder(new EntityAttributeModifier(identifier, value, operation), slot, baseTarget));
            }
        }
    }

    /**
     * Generates a unique id for the slot to prevent collisions
     *
     * @param equipmentSlot
     * @return a unique ID for the slot
     */
    public static Identifier getIDforSlot(EquipmentSlot equipmentSlot, EntityAttribute attribute, EntityAttributeModifier.Operation operation) {
        return getIDforSlot(equipmentSlot, attribute, operation, "");
    }

    /**
     * Generates a unique id for the slot to prevent collisions
     *
     * @param equipmentSlot
     * @return a unique ID for the slot
     */
    public static Identifier getIDforSlot(EquipmentSlot equipmentSlot, EntityAttribute attribute, EntityAttributeModifier.Operation operation, String context) {
        String slotidString = equipmentSlot.getName() + "-" + attribute.getTranslationKey() + "-" + equipmentSlot.getEntitySlotId() + "-" + equipmentSlot.getArmorStandSlotId() + "-" + operation.toString() + context;
        return getIDforSlot(slotidString);
    }

    public static Identifier getIDforSlot(String slotidString) {
        return Miapi.MiapiIdentifier(slotidString);
    }

    private static EntityAttributeModifier.Operation getOperation(String operationString) {
        if (operationString == null) {
            return EntityAttributeModifier.Operation.ADD_VALUE;
        }
        return switch (operationString) {
            case "*" -> EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE;
            case "**" -> EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
            default -> EntityAttributeModifier.Operation.ADD_VALUE;
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

    public record EntityAttributeModifierHolder(EntityAttributeModifier attributeModifier, EquipmentSlot slot,
                                                EntityAttributeModifier.Operation mergeTo) {
    }

    public interface AttributeTransformer {
        Multimap<EntityAttribute, EntityAttributeModifierHolder> transform(Multimap<EntityAttribute, EntityAttributeModifierHolder> map, ItemStack itemstack);
    }

    public interface IdentifierGetter {
        Identifier fromSlot(EquipmentSlot equipmentSlot, EntityAttribute attribute, EntityAttributeModifier.Operation operation);
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
