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
    Text text;
    double defaultValue;

    private AttributeSingleDisplay(EntityAttribute attribute, EquipmentSlot slot, Text text, Text hover, double defaultValue, String format) {
        super(0, 0, 80, 32, text, hover);
        this.slot = slot;
        this.attribute = attribute;
        this.defaultValue = defaultValue;
        this.text = text;
        modifierFormat = Util.make(new DecimalFormat(format), (decimalFormat) -> {
            decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
        });
    }

    @Override
    public double getValue(ItemStack stack){
        return AttributeRegistry.getAttribute(stack, attribute, slot, defaultValue);
    }

    @Override
    public boolean shouldRender(ItemStack original, ItemStack compareTo) {
        this.original = original;
        this.compareTo = compareTo;
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
        public Text name;
        public Text hoverDescription = Text.empty();
        public EquipmentSlot slot = EquipmentSlot.MAINHAND;
        public String translationKey = "";
        public Object[] descriptionArgs = new Object[]{};
        public String format = "##.##";

        private Builder(EntityAttribute attribute) {
            this.attribute = attribute;
        }

        public Builder setDefault(double defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder setTranslationKey(String key) {
            translationKey = key;
            name = Text.translatable(Miapi.MOD_ID + ".stat." + key);
            hoverDescription = Text.translatable(Miapi.MOD_ID + ".stat." + key + ".description", descriptionArgs);
            return this;
        }

        public Builder setDescriptionArguments(Object... args) {
            descriptionArgs = args;
            hoverDescription = Text.translatable(Miapi.MOD_ID + ".stat." + translationKey + ".description", descriptionArgs);
            return this;
        }

        public Builder setName(Text name) {
            this.name = name;
            return this;
        }

        public Builder setSlot(EquipmentSlot slot) {
            this.slot = slot;
            return this;
        }

        public Builder setHoverDescription(Text hoverDescription) {
            this.hoverDescription = hoverDescription;
            return this;
        }

        public Builder setFormat(String format) {
            this.format = format;
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
            return new AttributeSingleDisplay(attribute, slot, name, hoverDescription, defaultValue, format);
        }
    }
}
