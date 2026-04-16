package smartin.miapi.modules.properties.inventory.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import smartin.miapi.client.gui.ParentHandledScreen;
import smartin.miapi.modules.properties.inventory.SlotInfo;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public class InventoryScreen extends ParentHandledScreen<DefaultInventoryScreenHandler> {

    private SlotInfo hoveredInventory = null;
    public static int leftSlotInfSpace = 22;

    public SlotLayoutManager layoutManager;

    public InventoryScreen(DefaultInventoryScreenHandler menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, Component.empty());
        this.imageWidth = 196;
        this.imageHeight = 222;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 72;
    }

    @Override
    protected void init() {
        super.init();

        layoutManager = new SlotLayoutManager(
                menu.getManagedInventories(),
                menu.getManagedSlots(),
                6,
                leftSlotInfSpace,
                12,
                leftPos,
                topPos + 9
        );
    }

    public void resetHoverSlot() {
        this.hoveredSlot = null;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        layoutManager.scroll((int) -Math.signum(scrollY * 35));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);


        // Apply layout every frame
        //layoutManager.layout(false);

        super.render(context, mouseX, mouseY, delta);

        renderHighlights(context);

        // Render headers AFTER slots
        layoutManager.renderHeaders(context, mouseX, mouseY);
        updateHoveredInventory(context, mouseX, mouseY);
        this.renderTooltip(context, mouseX, mouseY);
    }

    @Override
    public void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        if (slot instanceof DefaultInventoryScreenHandler.ManagedSlot) {
            layoutManager.withScissor(guiGraphics, () -> {
                super.renderSlot(guiGraphics, slot);
            });
        } else {
            super.renderSlot(guiGraphics, slot);
        }
    }

    private void renderHighlights(GuiGraphics gfx) {
        if (hoveredInventory == null) return;

        int left = this.leftPos;
        int top = this.topPos;

        for (DefaultInventoryScreenHandler.ManagedInventory managed : menu.getManagedInventories()) {
            if (!hoveredInventory.equals(managed.slotInfo)) continue;

            int color = managed.slotInfo.getColor().argb();

            for (int i = managed.firstSlot; i <= managed.lastSlot; i++) {
                Slot slot = menu.getManagedSlots().get(i);

                if (!slot.isActive()) continue;

                gfx.fill(
                        left + slot.x - 1,
                        top + slot.y - 1,
                        left + slot.x + 17,
                        top + slot.y + 17,
                        color
                );
            }
        }
    }

    private void updateHoveredInventory(GuiGraphics context, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos + 7;

        hoveredInventory = null;

        int yOffset = 2;
        int height = 18;
        layoutManager.update(context, this, mouseX, mouseY);

        for (SlotInfo info : menu.getManagedInventories()
                .stream()
                .map(a -> a.slotInfo)
                .sorted(Comparator.comparingDouble(SlotInfo::priority))
                .collect(Collectors.toCollection(LinkedHashSet::new))) {

            int x1 = left;
            int x2 = left + height + 4;
            int y1 = top + yOffset;
            int y2 = y1 + height;

            int color = info.getColor().argb();

            context.fill(x1, y1, x2, y2, color);
            context.drawString(
                    Minecraft.getInstance().font,
                    info.getName(),
                    x1 - 20,
                    y1,
                    color,
                    false
            );

            if (mouseX >= x1 && mouseX <= x2 &&
                mouseY >= y1 && mouseY <= y2) {
                hoveredInventory = info;
            }

            yOffset += height;
        }

        // Apply filtering to layout manager
        if (hoveredInventory != null) {
            //layoutManager.setSelectedType(hoveredInventory);
        } else {
            //layoutManager.clearSelectedType();
        }
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float delta, int mouseX, int mouseY) {
        // Background only — layout handled by SlotLayoutManager
    }
}