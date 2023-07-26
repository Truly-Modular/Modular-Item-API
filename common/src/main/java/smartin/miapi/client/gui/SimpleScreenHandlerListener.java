package smartin.miapi.client.gui;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import org.apache.logging.log4j.util.TriConsumer;

public class SimpleScreenHandlerListener implements ScreenHandlerListener {
    protected final TriConsumer<ScreenHandler, Integer, ItemStack> onSlotUpdate;
    protected final TriConsumer<ScreenHandler, Integer, Integer> onPropertyUpdate;

    public SimpleScreenHandlerListener(TriConsumer<ScreenHandler, Integer, ItemStack> onSlotUpdate, TriConsumer<ScreenHandler, Integer, Integer> onPropertyUpdate) {
        this.onSlotUpdate = onSlotUpdate;
        this.onPropertyUpdate = onPropertyUpdate;
    }
    public SimpleScreenHandlerListener(TriConsumer<ScreenHandler, Integer, ItemStack> onSlotUpdate) {
        this.onSlotUpdate = onSlotUpdate;
        this.onPropertyUpdate = (a, b, c) -> {};
    }

    @Override
    public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
        onSlotUpdate.accept(handler, slotId, stack);
    }

    @Override
    public void onPropertyUpdate(ScreenHandler handler, int property, int value) {
        onPropertyUpdate.accept(handler, property, value);
    }
}
