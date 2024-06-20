package smartin.miapi.client.gui.crafting.statdisplay;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class JsonStatDisplay extends SingleStatDisplayDouble {
    SingleStatDisplayDouble.StatReaderHelper statReader;

    public JsonStatDisplay(StatListWidget.TextGetter title, StatListWidget.TextGetter description, SingleStatDisplayDouble.StatReaderHelper statReaderHelper, double min, double max) {
        super(0, 0, 51, 19, title, description);
        this.statReader = statReaderHelper;
        this.minValue = min;
        this.maxValue = max;
    }

    @Override
    public double getValue(ItemStack stack) {
        return statReader.getValue(stack);
    }

    @Override
    public boolean shouldRender(ItemStack original, ItemStack compareTo) {
        super.shouldRender(original, compareTo);
        if (statReader.hasValue(original)) {
            textWidget.setText(text.resolve(original));
            hoverDescription.setText(hover.resolve(original));
            return true;
        }
        if (statReader.hasValue(compareTo)) {
            textWidget.setText(text.resolve(compareTo));
            hoverDescription.setText(hover.resolve(compareTo));
            return true;
        }
        return false;
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public static class Builder {
        StatListWidget.TextGetter title;
        StatListWidget.TextGetter description;
        SingleStatDisplayDouble.StatReaderHelper statReaderHelper;
        double min = 0;
        double max = 10;
        public boolean isInversed = false;

        public Builder setTitle(Text title) {
            this.title = (stack) -> title;
            return this;
        }

        public Builder setDescription(Text description) {
            this.description = (stack) -> description;
            return this;
        }

        public Builder setStatReader(SingleStatDisplayDouble.StatReaderHelper statReaderHelper) {
            this.statReaderHelper = statReaderHelper;
            return this;
        }

        public Builder setMax(int max) {
            this.max = max;
            return this;
        }

        public Builder setMin(int min) {
            this.min = min;
            return this;
        }

        public Builder inverse(boolean inversed) {
            this.isInversed = inversed;
            return this;
        }

        public JsonStatDisplay build() {
            JsonStatDisplay jsonStatDisplay = new JsonStatDisplay(title, description, statReaderHelper, min, max);
            jsonStatDisplay.inverse = isInversed;
            return jsonStatDisplay;
        }
    }
}
