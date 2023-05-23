package smartin.miapi.client.gui.crafting.statdisplay;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import smartin.miapi.modules.properties.util.SimpleDoubleProperty;

public class SinglePropertyStatDisplay extends SingleStatDisplayDouble {
    protected SimpleDoubleProperty property;

    protected SinglePropertyStatDisplay(Text title, Text hover, SimpleDoubleProperty property) {
        super(0, 0, 80, 32, title, hover);
        this.property = property;
    }

    @Override
    public boolean shouldRender(ItemStack original, ItemStack compareTo) {
        this.original = original;
        this.compareTo = compareTo;
        if (property.hasValue(original)) {
            return true;
        }
        if (property.hasValue(compareTo)) {
            return true;
        }
        return false;
    }

    @Override
    public double getValue(ItemStack stack) {
        Double value = property.getValue(stack);
        if (value != null) return value;
        return 0;
    }

    public static Builder Builder(SimpleDoubleProperty property) {
        return new Builder(property);
    }

    public static class Builder {
        SimpleDoubleProperty property;
        public Text name;
        public Text hoverDescription;
        public String format = "##.##";

        private Builder(SimpleDoubleProperty property) {
            this.property = property;
        }

        public Builder setName(Text name) {
            this.name = name;
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

        public SinglePropertyStatDisplay build() {
            // Validate the required fields
            if (name == null) {
                throw new IllegalStateException("Name is required");
            }
            if (property == null) {
                throw new IllegalStateException("Property is required");
            }

            // Create an instance of AttributeProperty with the builder values
            return new SinglePropertyStatDisplay(name, hoverDescription, property);
        }
    }
}