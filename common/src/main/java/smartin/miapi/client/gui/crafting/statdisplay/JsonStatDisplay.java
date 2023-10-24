package smartin.miapi.client.gui.crafting.statdisplay;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import smartin.miapi.Miapi;

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
        Miapi.DEBUG_LOGGER.warn("original" + original);
        super.shouldRender(original, compareTo);
        if (statReader.hasValue(original)) {
            return true;
        }
        return statReader.hasValue(compareTo);
    }
}
