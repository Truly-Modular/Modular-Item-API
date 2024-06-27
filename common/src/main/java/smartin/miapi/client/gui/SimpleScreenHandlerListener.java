package smartin.miapi.client.gui;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.util.TriConsumer;

public class SimpleScreenHandlerListener implements ContainerListener {
    protected final TriConsumer<AbstractContainerMenu, Integer, ItemStack> onSlotUpdate;
    protected final TriConsumer<AbstractContainerMenu, Integer, Integer> onPropertyUpdate;

    public SimpleScreenHandlerListener(TriConsumer<AbstractContainerMenu, Integer, ItemStack> onSlotUpdate, TriConsumer<AbstractContainerMenu, Integer, Integer> onPropertyUpdate) {
        this.onSlotUpdate = onSlotUpdate;
        this.onPropertyUpdate = onPropertyUpdate;
    }
    public SimpleScreenHandlerListener(TriConsumer<AbstractContainerMenu, Integer, ItemStack> onSlotUpdate) {
        this.onSlotUpdate = onSlotUpdate;
        this.onPropertyUpdate = (a, b, c) -> {};
    }

    @Override
    public void slotChanged(AbstractContainerMenu handler, int slotId, ItemStack stack) {
        onSlotUpdate.accept(handler, slotId, stack);
    }

    @Override
    public void dataChanged(AbstractContainerMenu handler, int property, int value) {
        onPropertyUpdate.accept(handler, property, value);
    }
}
