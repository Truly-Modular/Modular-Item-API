package smartin.miapi.client.gui.crafting.statdisplay;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

public class BooleanStatDisplay extends SingleStatDisplayBoolean {
    public ComplexBooleanProperty property;


    public BooleanStatDisplay(StatListWidget.TextGetter title, StatListWidget.TextGetter hover, ComplexBooleanProperty property) {
        super(0, 0, 51, 19, title, hover);
        this.property = property;
    }

    @Override
    public double getValue(ItemStack stack) {
        return property.isTrue(stack) ? 1 : 0;
    }

    @Override
    public boolean shouldRender(ItemStack original, ItemStack compareTo) {
        return super.shouldRender(original, compareTo);
        //return property.hasValue(original) || property.hasValue(compareTo);
    }

    @Override
    public boolean getValueItemStack(ItemStack itemStack) {
        return property.isTrue(itemStack);
    }

    @Override
    public boolean hasValueItemStack(ItemStack itemStack) {
        return property.hasValue(itemStack);
    }

    public static Builder builder(ComplexBooleanProperty property) {
        return new Builder(property);
    }

    public static class Builder {
        ComplexBooleanProperty property;
        public StatListWidget.TextGetter name;
        public StatListWidget.TextGetter hoverDescription = (stack) -> Component.empty();
        public String translationKey = "";
        public Object[] descriptionArgs = new Object[]{};

        private Builder(ComplexBooleanProperty property) {
            this.property = property;
        }

        public Builder setName(Component name) {
            this.name = (stack) -> name;
            return this;
        }

        public Builder setName(StatListWidget.TextGetter name) {
            this.name = name;
            return this;
        }

        public Builder setTranslationKey(String key) {
            translationKey = key;
            name = (stack) -> Component.translatable(Miapi.MOD_ID + ".stat." + key, SingleStatDisplayBoolean.getText(property.isTrue(stack)));
            hoverDescription = (stack) -> Component.translatable(Miapi.MOD_ID + ".stat." + key + ".description"+ SingleStatDisplayBoolean.getText(property.isTrue(stack)));
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

        public BooleanStatDisplay build() {
            // Validate the required fields
            if (name == null) {
                throw new IllegalStateException("Name is required");
            }
            if (property == null) {
                throw new IllegalStateException("Property is required");
            }

            // Create an instance of AttributeProperty with the builder values
            BooleanStatDisplay display = new BooleanStatDisplay(name, hoverDescription, property);
            return display;
        }
    }
}
