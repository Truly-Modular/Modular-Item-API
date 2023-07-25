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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Environment(EnvType.CLIENT)
public class AttributeSingleDisplay extends SingleStatDisplayDouble {
    final EntityAttribute attribute;
    final EquipmentSlot slot;
    double defaultValue;

    private AttributeSingleDisplay(EntityAttribute attribute, EquipmentSlot slot, StatDisplay.TextGetter text, StatDisplay.TextGetter hover, double defaultValue, DecimalFormat modifierFormat) {
        super(0, 0, 80, 32, text, hover);
        this.slot = slot;
        this.attribute = attribute;
        this.defaultValue = defaultValue;
        this.modifierFormat = modifierFormat;
    }

    @Override
    public double getValue(ItemStack stack) {
        return AttributeRegistry.getAttribute(stack, attribute, slot, defaultValue);
    }

    @Override
    public boolean shouldRender(ItemStack original, ItemStack compareTo) {
        super.shouldRender(original, compareTo);
        if (original.getAttributeModifiers(slot).containsKey(attribute)) {
            return true;
        }
        if (compareTo.getAttributeModifiers(slot).containsKey(attribute)) {
            return true;
        }
        return false;
    }

    public static Builder Builder(EntityAttribute attribute) {
        return new Builder(attribute);
    }

    public static class Builder {
        EntityAttribute attribute;
        public double defaultValue = 1;
        public StatDisplay.TextGetter name;
        public StatDisplay.TextGetter hoverDescription = (stack) -> Text.empty();
        public EquipmentSlot slot = EquipmentSlot.MAINHAND;
        public String translationKey = "";
        public Object[] descriptionArgs = new Object[]{};
        public DecimalFormat modifierFormat;
        public double min = 0;
        public double max = 100;

        private Builder(EntityAttribute attribute) {
            this.attribute = attribute;
            modifierFormat = Util.make(new DecimalFormat("##.##"), (decimalFormat) -> {
                decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
            });
        }

        public Builder setMax(double maxValue){
            max = maxValue;
            return this;
        }

        public Builder setMin(double minValue){
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

        public Builder setName(StatDisplay.TextGetter name) {
            this.name = name;
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

        public Builder setHoverDescription(StatDisplay.TextGetter hoverDescription) {
            this.hoverDescription = hoverDescription;
            return this;
        }

        public Builder setFormat(String format) {
            modifierFormat = Util.make(new DecimalFormat(format), (decimalFormat) -> {
                decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
            });
            return this;
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
            return display;
        }
    }
}
