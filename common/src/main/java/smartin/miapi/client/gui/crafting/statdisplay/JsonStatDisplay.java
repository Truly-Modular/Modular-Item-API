package smartin.miapi.client.gui.crafting.statdisplay;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;

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
            return true;
        }
        textWidget.setText(text.resolve(compareTo));
        hoverDescription.setText(hover.resolve(compareTo));
        return statReader.hasValue(compareTo);
    }
}
