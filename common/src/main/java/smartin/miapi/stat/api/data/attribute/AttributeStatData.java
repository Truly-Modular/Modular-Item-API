package smartin.miapi.stat.api.data.attribute;

import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.stat.api.StatMetaData;
import smartin.miapi.modules.properties.attributes.AttributeProperty;
import smartin.miapi.modules.properties.attributes.AttributeToolTipHelper;
import smartin.miapi.modules.properties.attributes.AttributeUtil;
import smartin.miapi.modules.properties.attributes.EquipmentSlotGroupWrapper;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Optional;

import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.*;

public record AttributeStatData(
        Holder<Attribute> attribute,
        @Nullable EquipmentSlot slot,
        AttributeModifier.Operation operation,
        DecimalFormat format,
        double defaultValue,
        double min,
        double max,
        double fallbackValue,
        double priority
) implements StatMetaData<Double> {

    @Override
    public boolean hasStat(ItemStack itemStack) {
        Attribute attribute = this.attribute.value();

        ItemAttributeModifiers modifiers = itemStack.getOrDefault(
                DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.EMPTY);

        boolean hasVanilla = modifiers.modifiers().stream().anyMatch(entry -> {
            AttributeModifier modifier = entry.modifier();
            if (!entry.attribute().value().equals(attribute)) {
                return false;
            }
            if (modifier.amount() == 0) {
                return false;
            }
            return matchesOperation(modifier.operation());
        });
        if (hasVanilla) {
            return true;
        }
        return AttributeProperty.buildMiapiModifiers(itemStack).stream().anyMatch(modifier ->
                modifier.attribute().equals(attribute)
                        && modifier.operation() == operation
                        && modifier.value().getValue() != 0
        );
    }

    public double getPriority() {
        return priority;
    }

    @Override
    public Double getData(ItemStack stack) {
        Attribute attribute = this.attribute.value();
        if (slot != null) {
            return AttributeUtil.getActualValue(
                    stack,
                    slot,
                    attribute,
                    fallbackValue
            );
        }

        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            Multimap<Attribute, AttributeModifier> currentSlot = AttributeUtil.getAttribute(stack, equipmentSlot);
            if (!currentSlot.containsKey(attribute)) {
                continue;
            }

            double value;

            switch (operation) {
                case ADD_VALUE -> {
                    value = defaultValue;
                    for (AttributeModifier modifier : currentSlot.get(attribute)) {
                        if (modifier.operation().equals(operation)) {
                            value += modifier.amount();
                        }
                    }
                    if (value != 0) {
                        return value;
                    }
                }

                case ADD_MULTIPLIED_BASE -> {
                    value = 0.0;
                    for (AttributeModifier modifier : currentSlot.get(attribute)) {
                        if (modifier.operation().equals(ADD_MULTIPLIED_BASE)) {
                            value += modifier.amount();
                        }
                    }
                    for (AttributeModifier modifier : currentSlot.get(attribute)) {
                        if (modifier.operation().equals(ADD_MULTIPLIED_TOTAL)) {
                            value = (value + 1) * (modifier.amount() + 1) - 1;
                        }
                    }
                    if (value != 0) {
                        return value * 100;
                    }
                }
                case ADD_MULTIPLIED_TOTAL -> {
                    value = 1.0;

                    for (AttributeModifier modifier : currentSlot.get(attribute)) {
                        if (modifier.operation().equals(operation)) {
                            value *= modifier.amount();
                        }
                    }
                    if (value != 0) {
                        return value * 100;
                    }
                }
            }
        }

        for (String customSlot : AttributeToolTipHelper.customSlots) {
            double attributeValue = AttributeProperty.buildMiapiModifiers(stack).stream()
                    .filter(modifier ->
                            modifier.attribute().equals(attribute)
                                    && modifier.slot().raw().equals(customSlot)
                                    && modifier.operation() == operation
                    )
                    .mapToDouble(modifier -> modifier.value().getValue())
                    .sum();

            if (attributeValue != 0) {
                return operation.equals(ADD_VALUE)
                        ? attributeValue
                        : attributeValue * 100;
            }
        }

        return fallbackValue;
    }

    private boolean matchesOperation(AttributeModifier.Operation modifierOperation) {
        return modifierOperation.equals(operation)
                || (operation.equals(ADD_MULTIPLIED_BASE)
                && modifierOperation.equals(ADD_MULTIPLIED_TOTAL));
    }

    /**
     * Returns the MIAPI resolvable behind this attribute, when one exists.
     *
     * This is separate from getData(): the actual stat value is a Double,
     * while the resolvable provides modifier/source information.
     */
    public Optional<DoubleOperationResolvable> getResolvable(ItemStack stack) {
        Attribute attribute = this.attribute.value();

        Optional<
                Map<
                        net.minecraft.resources.ResourceLocation,
                        Map<
                                AttributeModifier.Operation,
                                Map<EquipmentSlotGroupWrapper, DoubleOperationResolvable>
                        >
                >
                > optional = AttributeProperty.property.getData(stack);

        if (optional.isPresent()) {
            var attributeMap = optional.get();
            var attributeKey = BuiltInRegistries.ATTRIBUTE.getKey(attribute);

            if (attributeKey != null) {
                var operationMap = attributeMap.get(attributeKey);

                if (operationMap != null) {
                    var resolvableMap = operationMap.get(operation);

                    if (resolvableMap != null) {
                        for (var entry : resolvableMap.entrySet()) {
                            EquipmentSlotGroupWrapper group = entry.getKey();

                            if (group.group().isPresent()) {
                                if ((slot == null || group.group().get().test(slot))
                                        && entry.getValue().getValue() != 0) {
                                    return Optional.of(entry.getValue());
                                }
                            }
                        }
                    }
                }
            }
        }

        return AttributeProperty.buildMiapiModifiers(stack).stream()
                .filter(modifier ->
                        modifier.attribute().equals(attribute)
                                && modifier.operation().equals(operation)
                                && modifier.value().getValue() != 0
                )
                .map(AttributeProperty.MiapiAttributeModifier::value)
                .findFirst();
    }
}