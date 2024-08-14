package smartin.miapi.modules.properties.attributes;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import smartin.miapi.Miapi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class AttributeUtil {


    public static Multimap<Attribute, AttributeModifier> getAttribute(ItemStack itemStack, EquipmentSlot equipmentSlot) {
        Multimap<Attribute, AttributeModifier> multimap = ArrayListMultimap.create();
        ItemAttributeModifiers attributeModifiers = itemStack.getComponents().get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (attributeModifiers != null) {
            attributeModifiers.forEach(equipmentSlot, ((attributeHolder, attributeModifier) ->
                    multimap.put(attributeHolder.value(), attributeModifier)));
        }
        return multimap;
    }

    public static Multimap<Attribute, AttributeModifier> getAttribute(ItemStack itemStack, EquipmentSlotGroup equipmentSlot) {
        Multimap<Attribute, AttributeModifier> multimap = ArrayListMultimap.create();
        ItemAttributeModifiers attributeModifiers = itemStack.getComponents().get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (attributeModifiers != null) {
            attributeModifiers.forEach(equipmentSlot, ((attributeHolder, attributeModifier) ->
                    multimap.put(attributeHolder.value(), attributeModifier)));
        }
        return multimap;
    }

    /**
     * A function to sort the multimap to provide better view in the gui.
     * Sorting is based on the {@link AttributeProperty#priorityMap}
     *
     * @param multimap
     * @return
     */
    public static Multimap<Holder<Attribute>, AttributeModifier> sortMultimap(Multimap<Holder<Attribute>, AttributeModifier> multimap) {
        Comparator<Holder<Attribute>> comparator = (attribute1, attribute2) -> {
            // Get the priority values for the attributes, using 0 as the default value
            float priority1 = AttributeProperty.priorityMap.getOrDefault(attribute1.value(), 0f);
            float priority2 = AttributeProperty.priorityMap.getOrDefault(attribute2.value(), 0f);

            // Sort in ascending order (lower priority values first)
            return Float.compare(priority1, priority2);
        };

        // Sort the keys (attributes) of the Multimap using the comparator
        List<Holder<Attribute>> sortedKeys = new ArrayList<>(multimap.keySet());
        sortedKeys.sort(comparator);

        // Create a new Multimap with the sorted keys
        Multimap<Holder<Attribute>, AttributeModifier> sortedMultimap = LinkedListMultimap.create();

        // Iterate over the sorted keys and add the corresponding values to the sorted Multimap
        for (Holder<Attribute> attribute : sortedKeys) {
            sortedMultimap.putAll(attribute, multimap.get(attribute));
        }

        // Clear the original Multimap and add the sorted entries
        return sortedMultimap;
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
        ItemAttributeModifiers attributeModifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (attributeModifiers != null) {
            attributeModifiers.forEach(slot, (attribute, modifier) -> {
                if (entityAttribute.equals(attribute)) {
                    modifiers.add(modifier);
                }
            });
        }
        return getActualValue(modifiers, fallback);
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

    /**
     * Generates a unique id for the slot to prevent collisions
     *
     * @param equipmentSlot
     * @return a unique ID for the slot
     */
    public static ResourceLocation getIDForSlot(EquipmentSlotGroup equipmentSlot, Attribute attribute, AttributeModifier.Operation operation) {
        return getIDForSlot(equipmentSlot, attribute, operation, "");
    }

    /**
     * Generates a unique id for the slot to prevent collisions
     *
     * @param equipmentSlot
     * @return a unique ID for the slot
     */
    public static ResourceLocation getIDForSlot(EquipmentSlotGroup equipmentSlot, Attribute attribute, AttributeModifier.Operation operation, String context) {
        String slotidString = equipmentSlot.getSerializedName() + "-" + attribute.getDescriptionId() + "-" + equipmentSlot.name() + "-" + equipmentSlot.ordinal() + "-" + operation.toString() + context;
        return getIDForSlot(slotidString);
    }

    public static ResourceLocation getIDForSlot(String slotidString) {
        return Miapi.id(slotidString);
    }
}
