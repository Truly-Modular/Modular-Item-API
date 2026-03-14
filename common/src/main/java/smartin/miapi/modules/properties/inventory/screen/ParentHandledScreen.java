package smartin.miapi.modules.properties.inventory.screen;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import smartin.miapi.modules.properties.inventory.InventoryType;
import smartin.miapi.modules.properties.inventory.ItemInventoryManager;
import smartin.miapi.modules.properties.inventory.SlotInfo;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public class ParentHandledScreen extends smartin.miapi.client.gui.ParentHandledScreen<DefaultInventoryScreenHandler> {

    private SlotInfo hoveredInventory = null;
    public static int leftSlotInfSpace = 18;

    public ParentHandledScreen(DefaultInventoryScreenHandler menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, Component.empty());
        this.imageWidth = 196;
        this.imageHeight = 222;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 72;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        // update hoveredInventory based on sidebar
        updateHoveredInventory(context, mouseX, mouseY);

        super.render(context, mouseX, mouseY, delta);
        this.renderTooltip(context, mouseX, mouseY);
    }


    private void updateHoveredInventory(GuiGraphics context, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos + 7;

        hoveredInventory = null;
        int yOffset = 2;
        int height = 18;
        for (SlotInfo info : menu.getManagedInventories()
                .stream().map(a -> a.slotInfo)
                .sorted(Comparator.comparingDouble(SlotInfo::priority))
                .collect(Collectors.toCollection(LinkedHashSet::new))) {
            int sidebarX1 = left;
            int sidebarX2 = left + height + 4;
            int sidebarY1 = top + yOffset;
            int sidebarY2 = sidebarY1 + height;
            context.fill(sidebarX1, sidebarY1, sidebarX2, sidebarY2, info.getColor().argb());
            context.drawString(Minecraft.getInstance().font, info.getName(), sidebarX1 - 20, sidebarY1, info.getColor().argb(), false);

            if (mouseX >= sidebarX1 && mouseX <= sidebarX2 &&
                mouseY >= sidebarY1 && mouseY <= sidebarY2) {
                hoveredInventory = info;
            }

            yOffset += height;
        }
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float delta, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        int yOffset = 18;

        for (InventoryType type : ItemInventoryManager.INVENTORY_TYPES.values()) {

            // draw header
            boolean hasValue = false;
            int originalOffset = yOffset;

            // gather SlotInfos for this type
            for (DefaultInventoryScreenHandler.ManagedInventory managed : menu.getManagedInventories()) {
                if (managed.type != type) continue;

                Color c = managed.slotInfo.getColor();
                int color = c.argb();

                int size = managed.container.getContainerSize();
                if (size > 0) {
                    hasValue = true;
                }
                int rows = (int) Math.ceil(size / 9.0);
                int height = rows * 18;

                // highlight slots if hovered
                if (hoveredInventory != null && hoveredInventory.equals(managed.slotInfo)) {
                    for (int i = managed.firstSlot; i <= managed.lastSlot; i++) {
                        Slot slot = menu.slots.get(i);
                        gfx.fill(left + slot.x - 1, top + slot.y - 1, left + slot.x + 17, top + slot.y + 17, color);
                    }
                }

                yOffset += height;
            }
            if (hasValue) {
                gfx.drawString(this.font, type.getName(), left + 8 + leftSlotInfSpace, top + originalOffset - 10, 0xFFFFFF, false);
                yOffset += 12; // spacing after each type
            }
        }
        this.inventoryLabelY = yOffset - 10;

        // render player inventory normally
    }
}