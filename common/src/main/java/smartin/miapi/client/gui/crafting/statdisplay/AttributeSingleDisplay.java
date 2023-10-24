package smartin.miapi.client.gui.crafting.statdisplay;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.modules.properties.AttributeProperty;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class AttributeSingleDisplay extends SingleStatDisplayDouble {
    public static Set<EntityAttribute> attributesWithDisplay = new HashSet<>();
    final EntityAttribute attribute;
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

    private AttributeSingleDisplay(EntityAttribute attribute, EquipmentSlot slot, StatListWidget.TextGetter text, StatListWidget.TextGetter hover, double defaultValue, DecimalFormat modifierFormat) {
        super(0, 0, 51, 19, text, hover);
        attributesWithDisplay.add(attribute);
        this.slot = slot;
        this.attribute = attribute;
        this.defaultValue = defaultValue;
        this.modifierFormat = modifierFormat;
    }

    @Override
    public double getValue(ItemStack stack) {
        return this.valueReader.getValue(stack);
    }


    public double getValueFunction(ItemStack stack) {
        if (slot == null) {
            Double value = null;
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                if (stack.getAttributeModifiers(equipmentSlot).containsKey(attribute)) {
                    if (inverse) {
                        if (value != null) {
                            value = Math.min(value, AttributeRegistry.getAttribute(stack, attribute, equipmentSlot, defaultValue));
                        } else {
                            value = AttributeRegistry.getAttribute(stack, attribute, equipmentSlot, defaultValue);
                        }
                    } else {
                        if (value != null) {
                            value = Math.max(value, AttributeRegistry.getAttribute(stack, attribute, equipmentSlot, defaultValue));
                        } else {
                            value = AttributeRegistry.getAttribute(stack, attribute, equipmentSlot, defaultValue);
                        }
                    }
                }
            }
            return value != null ? value : 0;
        }
        return AttributeRegistry.getAttribute(stack, attribute, slot, defaultValue);
    }

    @Override
    public boolean shouldRender(ItemStack original, ItemStack compareTo) {
        super.shouldRender(original, compareTo);
        if(valueReader.hasValue(original)){
            return true;
        }
        return valueReader.hasValue(compareTo);
    }

    public boolean hasAttribute(ItemStack itemStack){
        if (slot == null) {
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                if (
                        itemStack.getAttributeModifiers(equipmentSlot).containsKey(attribute) &&
                                AttributeProperty.getActualValue(itemStack, equipmentSlot, attribute) != attribute.getDefaultValue()) {
                    return true;
                }
            }
        }
        else{
            if (itemStack.getAttributeModifiers(slot).containsKey(attribute)) {
                return true;
            }
        }
        return false;
    }

    public static Builder builder(EntityAttribute attribute) {
        return new Builder(attribute);
    }

    public static class Builder {
        EntityAttribute attribute;
        public EquipmentSlot slot;
        public double defaultValue = 1;
        public StatListWidget.TextGetter name;
        public StatListWidget.TextGetter hoverDescription = (stack) -> Text.empty();
        public String translationKey = "";
        public Object[] descriptionArgs = new Object[]{};
        public DecimalFormat modifierFormat;
        public double min = 0;
        public double max = 100;
        public boolean inverse = false;
        public ValueGetter valueGetter;

        private Builder(EntityAttribute attribute) {
            this.attribute = attribute;
            name = (itemStack) -> Text.translatable(attribute.getTranslationKey());
            modifierFormat = Util.make(new DecimalFormat("##.##"), (decimalFormat) -> {
                decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
            });
            defaultValue = attribute.getDefaultValue();
            max = Math.min(2048, attribute.clamp(Double.MAX_VALUE));
            min = Math.max(-2048, attribute.clamp(Double.MIN_VALUE));
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
            name = (stack) -> Text.translatable(Miapi.MOD_ID + ".stat." + key);
            hoverDescription = (stack) -> Text.translatable(Miapi.MOD_ID + ".stat." + key + ".description", descriptionArgs);
            return this;
        }

        public Builder setName(Text name) {
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

        public Builder setHoverDescription(Text hoverDescription) {
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

        public Builder setValueGetter(ValueGetter reader){
            valueGetter = reader;
            return this;
        }

        interface ValueGetter {
            double getValue(ItemStack itemStack);
        }

        public AttributeSingleDisplay build() {
            // Validate the required fields
            if (name == null) {
                throw new IllegalStateException("Name is required");
            }
            if (attribute == null) {
                throw new IllegalStateException("Attribute is required");
            }

            // Create an instance of AttributeProperty with the builder values
            AttributeSingleDisplay display = new AttributeSingleDisplay(attribute, slot, name, hoverDescription, defaultValue, modifierFormat);
            display.minValue = min;
            display.maxValue = max;
            display.setInverse(inverse);
            if(valueGetter !=null){
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
            return display;
        }
    }
}
