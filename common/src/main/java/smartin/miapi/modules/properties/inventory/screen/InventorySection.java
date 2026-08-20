package smartin.miapi.modules.properties.inventory.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import smartin.miapi.modules.properties.inventory.InventoryType;

import java.awt.*;
import java.util.List;

public class InventorySection {

    public static final int SLOT_SIZE = 18;
    public static final int COLUMNS = 9;
    public static final int HEADER_HEIGHT = 9;

    private final InventoryType type;
    private final List<DefaultInventoryScreenHandler.ManagedInventory> inventories;
    private final List<DefaultInventoryScreenHandler.ManagedSlot> allSlots;

    private final int rowCount;

    // Cached dimensions.
    private final int contentHeight;
    private final int heightWithHeader;

    public InventorySection(
            InventoryType type,
            List<DefaultInventoryScreenHandler.ManagedInventory> inventories,
            List<DefaultInventoryScreenHandler.ManagedSlot> allSlots
    ) {
        this.type = type;
        this.inventories = List.copyOf(inventories);
        this.allSlots = allSlots;

        int count = 0;

        for (DefaultInventoryScreenHandler.ManagedInventory inventory : inventories) {
            count += inventory.lastSlot - inventory.firstSlot + 1;
        }

        this.rowCount = (int) Math.ceil(count / (double) COLUMNS);

        this.contentHeight = rowCount * SLOT_SIZE;
        this.heightWithHeader = HEADER_HEIGHT + contentHeight;
    }

    public InventoryType getType() {
        return type;
    }

    public List<DefaultInventoryScreenHandler.ManagedInventory> getInventories() {
        return inventories;
    }

    public int getRequiredHeight() {
        return heightWithHeader;
    }

    /**
     * Positions this section's slots.
     *
     * @param startX        section X position
     * @param startY        section Y position
     * @param scrollOffset  global scroll offset
     * @param visibleHeight visible content height
     */
    public void layout(
            int startX,
            int startY,
            int scrollOffset,
            int visibleHeight
    ) {
        int yCursor = startY + HEADER_HEIGHT;
        int index = 0;

        for (DefaultInventoryScreenHandler.ManagedInventory inventory : inventories) {
            int slotCount = inventory.lastSlot - inventory.firstSlot + 1;

            for (int i = 0; i < slotCount; i++) {
                DefaultInventoryScreenHandler.ManagedSlot slot =
                        allSlots.get(inventory.firstSlot + i);

                int row = index / COLUMNS;
                int col = index % COLUMNS;

                int localY = yCursor + row * SLOT_SIZE;
                int visibleY = localY - scrollOffset;

                boolean visible =
                        visibleY >= -SLOT_SIZE &&
                        visibleY < visibleHeight;

                slot.setEnabled(visible);

                if (visible) {
                    slot.setPos(
                            startX + col * SLOT_SIZE,
                            yCursor + visibleY
                    );
                }

                index++;
            }
        }
    }

    public void renderHeader(
            GuiGraphics graphics,
            int x,
            int sectionY,
            int scrollOffset,
            int visibleHeight
    ) {
        int visibleY = sectionY - scrollOffset;

        if (visibleY < -HEADER_HEIGHT || visibleY >= visibleHeight) {
            return;
        }

        graphics.drawString(
                Minecraft.getInstance().font,
                type.getName(),
                x,
                visibleY,
                Color.DARK_GRAY.getRGB(),
                false
        );
    }
}