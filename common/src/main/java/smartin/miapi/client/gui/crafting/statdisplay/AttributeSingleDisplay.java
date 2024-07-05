package smartin.miapi.client.gui.crafting.statdisplay;

import com.google.common.collect.Multimap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.attributes.AttributeUtil;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;

@Environment(EnvType.CLIENT)
public class AttributeSingleDisplay extends SingleStatDisplayDouble {
    public static Set<Attribute> attributesWithDisplay = new HashSet<>();
    public static Map<EquipmentSlot, Multimap<Attribute, AttributeModifier>> oldItemCache = new WeakHashMap<>();
    public static Map<EquipmentSlot, Multimap<Attribute, AttributeModifier>> compareItemCache = new WeakHashMap<>();
    public AttributeModifier.Operation operation = AttributeModifier.Operation.ADD_VALUE;
    final Attribute attribute;
    final EquipmentSlot slot;
    double defaultValue;
    public StatReaderHelper valueReader = new StatReaderHelper() {
        @Override
        public double getValue(ItemStack itemStack) {
            return getValueFunction(itemStack);
        }

        @Override
        public boolean hasValue(ItemStack itemStack) {
            return hasAttribute(itemStack);
        }
    };

    private AttributeSingleDisplay(Attribute attribute, EquipmentSlot slot, StatListWidget.TextGetter text, StatListWidget.TextGetter hover, double defaultValue, DecimalFormat modifierFormat) {
        super(0, 0, 51, 19, text, hover);
        attributesWithDisplay.add(attribute);
        this.slot = slot;
        this.attribute = attribute;
        this.defaultValue = defaultValue;
        this.modifierFormat = modifierFormat;
    }

    @Override
    public double getValue(ItemStack stack) {
        double value = this.valueReader.getValue(stack);
        if (!this.valueReader.hasValue(stack) || Double.isNaN(value)) {
            return attribute.getDefaultValue();
        }
        return value;
    }


    public double getValueFunction(ItemStack stack) {
        Map<EquipmentSlot, Multimap<Attribute, AttributeModifier>> attributeCache = compareItemCache;
        if (stack.equals(original)) {
            attributeCache = oldItemCache;
        }
        if (slot == null) {
            Double value = null;
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                Multimap<Attribute, AttributeModifier> currentSlot = attributeCache.get(equipmentSlot);
                if (attributeCache.get(equipmentSlot).containsKey(attribute)) {
                    switch (operation) {
                        case ADD_VALUE -> {
                            value = defaultValue;
                            for (AttributeModifier modifier : currentSlot.get(attribute).stream().filter(a -> a.operation().equals(operation)).toList()) {
                                value += modifier.amount();
                            }
                            return value;
                        }
                        case ADD_MULTIPLIED_BASE -> {
                            value = 0.0;
                            for (AttributeModifier modifier : currentSlot.get(attribute).stream().filter(a -> a.operation().equals(ADD_MULTIPLIED_BASE)).toList()) {
                                value += modifier.amount();
                            }
                            for (AttributeModifier modifier : currentSlot.get(attribute).stream().filter(a -> a.operation().equals(ADD_MULTIPLIED_TOTAL)).toList()) {
                                value = (value + 1) * (modifier.amount() + 1) - 1;
                            }
                            return value * 100;
                        }
                        case ADD_MULTIPLIED_TOTAL -> {
                            value = 1.0;
                            for (AttributeModifier modifier : currentSlot.get(attribute).stream().filter(a -> a.operation().equals(operation)).toList()) {
                                value = value * modifier.amount();
                            }
                            return value * 100;
                        }
                    }
                }
            }
            return defaultValue;
        }
        return AttributeUtil.getActualValue(attributeCache.get(slot), attribute, defaultValue);
    }

    @Override
    public boolean shouldRender(ItemStack original, ItemStack compareTo) {
        super.shouldRender(original, compareTo);
        if (valueReader.hasValue(original)) {
            return true;
        }
        return valueReader.hasValue(compareTo);
    }

    public boolean hasAttribute(ItemStack itemStack) {
        Map<EquipmentSlot, Multimap<Attribute, AttributeModifier>> attributeCache = compareItemCache;
        if (itemStack.equals(original)) {
            attributeCache = oldItemCache;
        }
        if (slot == null) {
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                Multimap<Attribute, AttributeModifier> slotMap = attributeCache.get(equipmentSlot);
                if (slotMap != null) {
                    Collection<AttributeModifier> attrCollection = attributeCache.get(equipmentSlot).get(attribute).stream().filter(attribute -> attribute.operation().equals(operation)).toList();
                    if (hasValue(attrCollection)) {
                        return true;
                    }
                }
            }
        } else {
            Multimap<Attribute, AttributeModifier> slotMap = attributeCache.get(slot);
            if (slotMap != null) {
                Collection<AttributeModifier> attrCollection = attributeCache.get(slot).get(attribute);
                return hasValue(attrCollection);
            }
        }
        return false;
    }

    public boolean hasValue(Collection<AttributeModifier> list) {
        list = list.stream().filter(attribute -> attribute.operation().equals(operation)).toList();
        if (operation.equals(AttributeModifier.Operation.ADD_VALUE)) {
            double value = AttributeUtil.getActualValue(list, attribute.getDefaultValue());
            if (
                    value != attribute.getDefaultValue() && !Double.isNaN(value)
            ) {
                return true;
            }
        } else {
            return !list.isEmpty();
        }
        return false;
    }

    public static Builder builder(Holder<Attribute> attribute) {
        return new Builder(attribute);
    }

    public static class Builder {
        Holder<Attribute> attribute;
        public EquipmentSlot slot;
        public double defaultValue = 1;
        public StatListWidget.TextGetter name;
        public StatListWidget.TextGetter hoverDescription = (stack) -> Component.empty();
        public String translationKey = "";
        public Object[] descriptionArgs = new Object[]{};
        public DecimalFormat modifierFormat;
        public double min = 0;
        public double max = 100;
        public boolean inverse = false;
        public ValueGetter valueGetter;

        private Builder(Holder<Attribute> attribute) {
            this.attribute = attribute;
            name = (itemStack) -> Component.translatable(attribute.value().getDescriptionId());
            modifierFormat = Util.make(new DecimalFormat("##.##"), (decimalFormat) -> {
                decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
            });
            defaultValue = attribute.value().getDefaultValue();
            max = Math.min(2048, attribute.value().sanitizeValue(Double.MAX_VALUE));
            min = Math.max(-2048, attribute.value().sanitizeValue(Double.MIN_VALUE));
        }

        public Builder setMax(double maxValue) {
            max = maxValue;
            return this;
        }

        public Builder setMin(double minValue) {
            min = minValue;
            return this;
        }

        public Builder setDefault(double defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder setTranslationKey(String key) {
            translationKey = key;
            name = (stack) -> Component.translatable(Miapi.MOD_ID + ".stat." + key);
            hoverDescription = (stack) -> Component.translatable(Miapi.MOD_ID + ".stat." + key + ".description", descriptionArgs);
            return this;
        }

        public Builder setName(Component name) {
            this.name = (stack) -> name;
            return this;
        }

        public Builder setName(StatListWidget.TextGetter name) {
            this.name = name;
            return this;
        }

        public Builder inverseNumber(boolean inverse) {
            this.inverse = inverse;
            return this;
        }

        public Builder setSlot(EquipmentSlot slot) {
            this.slot = slot;
            return this;
        }

        public Builder setHoverDescription(Component hoverDescription) {
            this.hoverDescription = (stack) -> hoverDescription;
            return this;
        }

        public Builder setHoverDescription(StatListWidget.TextGetter hoverDescription) {
            this.hoverDescription = hoverDescription;
            return this;
        }

        public Builder setFormat(String format) {
            modifierFormat = Util.make(new DecimalFormat(format), (decimalFormat) -> {
                decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
            });
            return this;
        }

        public Builder setValueGetter(ValueGetter reader) {
            valueGetter = reader;
            return this;
        }

        interface ValueGetter {
            double getValue(ItemStack itemStack);
        }

        public AttributeSingleDisplay[] build() {
            AttributeSingleDisplay[] displays = new AttributeSingleDisplay[2];
            // Validate the required fields
            if (name == null) {
                throw new IllegalStateException("Name is required");
            }
            if (attribute == null) {
                throw new IllegalStateException("Attribute is required");
            }

            // Create an instance of AttributeProperty with the builder values
            AttributeSingleDisplay display = new AttributeSingleDisplay(attribute.value(), slot, name, hoverDescription, defaultValue, modifierFormat);
            display.minValue = min;
            display.maxValue = max;
            display.setInverse(inverse);
            if (valueGetter != null) {
                display.valueReader = new StatReaderHelper() {
                    @Override
                    public double getValue(ItemStack itemStack) {
                        return valueGetter.getValue(itemStack);
                    }

                    @Override
                    public boolean hasValue(ItemStack itemStack) {
                        return display.hasAttribute(itemStack);
                    }
                };
            }
            display.operation = AttributeModifier.Operation.ADD_VALUE;
            displays[0] = display;

            AttributeSingleDisplay displayMulBase = new AttributeSingleDisplay(attribute.value(), slot, name, hoverDescription, defaultValue, modifierFormat);
            displayMulBase.minValue = 0;
            displayMulBase.maxValue = 100;
            displayMulBase.setInverse(inverse);
            if (valueGetter != null) {
                displayMulBase.valueReader = new StatReaderHelper() {
                    @Override
                    public double getValue(ItemStack itemStack) {
                        return valueGetter.getValue(itemStack);
                    }

                    @Override
                    public boolean hasValue(ItemStack itemStack) {
                        return displayMulBase.hasAttribute(itemStack);
                    }
                };
            }
            displayMulBase.operation = ADD_MULTIPLIED_BASE;
            displayMulBase.postfix = Component.literal("%");
            displays[1] = displayMulBase;

            AttributeSingleDisplay displayMulTotal = new AttributeSingleDisplay(attribute.value(), slot, name, hoverDescription, defaultValue, modifierFormat);
            displayMulTotal.minValue = 0;
            displayMulTotal.maxValue = 100;
            displayMulTotal.setInverse(inverse);
            if (valueGetter != null) {
                displayMulTotal.valueReader = new StatReaderHelper() {
                    @Override
                    public double getValue(ItemStack itemStack) {
                        return valueGetter.getValue(itemStack);
                    }

                    @Override
                    public boolean hasValue(ItemStack itemStack) {
                        return displayMulTotal.hasAttribute(itemStack);
                    }
                };
            }
            displayMulTotal.operation = AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
            displayMulTotal.postfix = Component.literal("%");
            //displays[2] = displayMulTotal;

            return displays;
        }
    }
}
