package smartin.miapi.client.gui.crafting.statdisplay;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import smartin.miapi.Miapi;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class MiningLevelStatDisplay extends SingleStatDisplayDouble {
    public String type;

    public MiningLevelStatDisplay(String type, StatDisplay.TextGetter title, StatDisplay.TextGetter hover) {
        super(0, 0, 80, 32, title, hover);
        this.type = type;
    }

    @Override
    public double getValue(ItemStack stack) {
        return smartin.miapi.modules.properties.MiningLevelProperty.getMiningLevel(type, stack);
    }

    public boolean shouldRender(ItemStack original, ItemStack compareTo) {
        super.shouldRender(original, compareTo);
        return getValue(original) > 0 || getValue(compareTo) > 0;
    }

    public static Builder Builder(String type) {
        return new Builder(type);
    }

    public static class Builder {
        public StatDisplay.TextGetter name;
        public StatDisplay.TextGetter hoverDescription = (stack) -> Text.empty();
        public EquipmentSlot slot = EquipmentSlot.MAINHAND;
        public String translationKey = "";
        public Object[] descriptionArgs = new Object[]{};
        public DecimalFormat modifierFormat;
        public double min = 0;
        public double max = 4;
        public String type;

        private Builder(String type) {
            this.type = type;
            setTranslationKey("mining.level." + type);
            modifierFormat = Util.make(new DecimalFormat("##"), (decimalFormat) -> {
                decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
            });
        }

        public Builder setName(StatDisplay.TextGetter name) {
            this.name = name;
            return this;
        }

        public Builder setFormat(String format) {
            modifierFormat = Util.make(new DecimalFormat(format), (decimalFormat) -> {
                decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
            });
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

        public Builder setTranslationKey(String key) {
            translationKey = key;
            name = (stack) -> Text.translatable(Miapi.MOD_ID + ".stat." + key);
            hoverDescription = (stack) -> Text.translatable(Miapi.MOD_ID + ".stat." + key + ".description", descriptionArgs);
            return this;
        }

        public MiningLevelStatDisplay build() {
            // Validate the required fields
            if (name == null) {
                throw new IllegalStateException("Name is required");
            }

            // Create an instance of AttributeProperty with the builder values
            MiningLevelStatDisplay display = new MiningLevelStatDisplay(type, name, hoverDescription);
            display.minValue = min;
            display.maxValue = max;
            display.modifierFormat = modifierFormat;
            return display;
        }
    }
}
