package smartin.miapi.client.gui.crafting.statdisplay;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import smartin.miapi.modules.properties.FlexibilityProperty;

public class FlexibilityStatDisplay extends SingleStatDisplayDouble {
    public FlexibilityStatDisplay() {
        super(0, 0, 80, 32, Text.literal("Flexibility"), Text.empty());
    }

    @Override
    public boolean shouldRender(ItemStack original, ItemStack compareTo) {
        this.original = original;
        this.compareTo = compareTo;
        if (FlexibilityProperty.getValue(original) != null) {
            return true;
        }
        if (FlexibilityProperty.getValue(compareTo) != null) {
            return true;
        }
        return false;
    }

    @Override
    public double getValue(ItemStack stack) {
        Double value = FlexibilityProperty.getValue(stack);
        if (value != null) return value;
        return 0;
    }
}
