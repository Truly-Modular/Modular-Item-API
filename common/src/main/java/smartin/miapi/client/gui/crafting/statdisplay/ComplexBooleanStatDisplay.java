package smartin.miapi.client.gui.crafting.statdisplay;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

public class ComplexBooleanStatDisplay extends SingleStatDisplayBoolean {
    public ComplexBooleanProperty property;


    public ComplexBooleanStatDisplay(StatListWidget.TextGetter title, StatListWidget.TextGetter hover, ComplexBooleanProperty property) {
        super(0, 0, 51, 19, title, hover);
        this.property = property;
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
        public StatListWidget.TextGetter hoverDescription = (stack) -> Text.empty();
        public String translationKey = "";
        public Object[] descriptionArgs = new Object[]{};

        private Builder(ComplexBooleanProperty property) {
            this.property = property;
        }

        public Builder setName(Text name) {
            this.name = (stack) -> name;
            return this;
        }

        public Builder setName(StatListWidget.TextGetter name) {
            this.name = name;
            return this;
        }

        public Builder setTranslationKey(String key) {
            translationKey = key;
            name = (stack) -> Text.translatable(Miapi.MOD_ID + ".stat." + key, SingleStatDisplayBoolean.getText(property.getValueSafe(stack)));
            hoverDescription = (stack) -> Text.translatable(Miapi.MOD_ID + ".stat." + key + ".description", SingleStatDisplayBoolean.getText(property.getValueSafe(stack)));
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

        public ComplexBooleanStatDisplay build() {
            // Validate the required fields
            if (name == null) {
                throw new IllegalStateException("Name is required");
            }
            if (property == null) {
                throw new IllegalStateException("Property is required");
            }

            // Create an instance of AttributeProperty with the builder values
            ComplexBooleanStatDisplay display = new ComplexBooleanStatDisplay(name, hoverDescription, property);
            return display;
        }
    }
}
