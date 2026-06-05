package smartin.miapi.modules.properties.inventory.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import smartin.miapi.modules.properties.inventory.InventoryType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public class SlotLayoutManager {

    private final List<DefaultInventoryScreenHandler.ManagedSlot> allSlots;
    private final List<DefaultInventoryScreenHandler.ManagedInventory> allInventories;

    private LinkedHashMap<InventoryType, List<DefaultInventoryScreenHandler.ManagedInventory>> inventoriesByType = new LinkedHashMap<>();
    private final LinkedHashMap<InventoryType, Integer> headerPositions = new LinkedHashMap<>();
    private final List<DefaultInventoryScreenHandler.ManagedSlot> activeSlots = new ArrayList<>();

    private InventoryType selectedType = null;

    // NOW PIXELS
    private int scrollOffset = 0;

    private final int visibleRows;
    private final int columns = 9;

    private int contentHeight = 0;

    private int startX;
    private int startY;
    private int textX;
    private int textY;

    private static final int SLOT_SIZE = 18;
    private static final int HEADER_SPACE = 9;

    public SlotLayoutManager(List<DefaultInventoryScreenHandler.ManagedInventory> inventories,
                             List<DefaultInventoryScreenHandler.ManagedSlot> slots,
                             int visibleRows,
                             int x,
                             int y,
                             int topX,
                             int topY) {
        this.allInventories = inventories;
        this.allSlots = slots;
        this.visibleRows = visibleRows;
        this.startX = x;
        this.startY = y;
        this.textX = topX;
        this.textY = topY;
        for (DefaultInventoryScreenHandler.ManagedInventory inv : allInventories) {
            inventoriesByType
                    .computeIfAbsent(inv.type, k -> new ArrayList<>())
                    .add(inv);
        }

        var list = inventoriesByType.entrySet().stream().sorted(Comparator.comparingDouble(c -> c.getKey().priority())).toList();
        inventoriesByType = new LinkedHashMap<>();
        list.forEach(entry -> inventoriesByType.put(entry.getKey(), entry.getValue()));
        layout(true);
    }

    public void setSelectedType(InventoryType type) {
        this.selectedType = type;
        this.scrollOffset = 0;
        layout(true);
    }

    public void clearSelectedType() {
        this.selectedType = null;
        this.scrollOffset = 0;
        layout(true);
    }

    public void scroll(int deltaPixels) {
        this.scrollOffset += deltaPixels;
        clampScroll();
        layout(true);
    }

    public void setScroll(int value) {
        this.scrollOffset = value;
        clampScroll();
        layout(true);
    }

    public void update(GuiGraphics context, InventoryScreen screen, int mouseX, int mouseY) {
        boolean isMouseOver = isMouseOver(mouseX, mouseY);
        if (screen.getFocusSlot() instanceof DefaultInventoryScreenHandler.ManagedSlot && !isMouseOver) {
            screen.resetHoverSlot();
        }
        //Miapi.LOGGER.info("disable "+isMouseOver);
        this.getActiveSlots().forEach(managedSlot ->
                managedSlot.setHighlightable(isMouseOver));
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        int scissorX = (textX + startX);
        int scissorY = (textY);
        int scissorW = (textX + startX + SLOT_SIZE * 9);
        int scissorH = (textY + SLOT_SIZE * visibleRows);
        boolean isMouseOver =
                mouseX > scissorX &&
                mouseX < scissorW &&
                mouseY > scissorY &&
                mouseY < scissorH;
        return isMouseOver;
    }

    private void clampScroll() {
        int maxScroll = Math.max(0, contentHeight - visibleRows * SLOT_SIZE);
        if (scrollOffset < 0) scrollOffset = 0;
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
    }

    public void layout(boolean applyPositions) {
        if (applyPositions) {
            for (var slot : allSlots) {
                slot.setEnabled(false);
            }
        }

        activeSlots.clear();
        headerPositions.clear();

        int yCursor = 0;

        for (var entry : inventoriesByType.entrySet()) {
            InventoryType type = entry.getKey();

            if (selectedType != null && type != selectedType) continue;

            boolean showHeader = selectedType == null;

            if (showHeader) {
                headerPositions.put(type, yCursor);
                yCursor += HEADER_SPACE;
            }

            int indexWithinType = 0;

            for (var inv : entry.getValue()) {
                int slotCount = inv.lastSlot - inv.firstSlot + 1;

                for (int i = 0; i < slotCount; i++) {
                    var slot = allSlots.get(inv.firstSlot + i);
                    activeSlots.add(slot);

                    int row = indexWithinType / columns;
                    int col = indexWithinType % columns;

                    int globalY = yCursor + row * SLOT_SIZE;
                    int visibleY = globalY - scrollOffset;

                    if (applyPositions) {
                        boolean visible = visibleY >= -SLOT_SIZE && visibleY < visibleRows * SLOT_SIZE;

                        slot.setEnabled(visible);
                        if (visible) {
                            int x = startX + col * SLOT_SIZE;
                            int y = startY + visibleY;
                            slot.setPos(x, y);
                        }
                    }

                    indexWithinType++;
                }
            }

            int rows = (int) Math.ceil(indexWithinType / (double) columns);
            yCursor += rows * SLOT_SIZE;
        }

        contentHeight = yCursor;
    }

    public void renderHeaders(GuiGraphics context, int mouseX, int mouseY) {
        if (selectedType != null) return;

        withScissor(context, () -> {
            for (var entry : headerPositions.entrySet()) {
                InventoryType type = entry.getKey();
                int globalY = entry.getValue();

                int visibleY = globalY - scrollOffset;

                boolean visible = visibleY >= -HEADER_SPACE && visibleY < visibleRows * SLOT_SIZE;
                if (!visible) continue;

                int x = startX;
                int y = startY + visibleY - HEADER_SPACE;

                context.drawString(
                        Minecraft.getInstance().font,
                        type.getName(),
                        x + textX,
                        y + textY,
                        0xFFFFFF,
                        false
                );
            }
        });
    }

    // SCISSOR WRAPPER
    public void withScissor(GuiGraphics context, Runnable render) {
        double scale = Minecraft.getInstance().getWindow().getGuiScale();
        int scissorX = (textX + startX);
        int scissorY = (textY);
        int scissorW = (textX + startX + SLOT_SIZE * 9);
        int scissorH = (textY + SLOT_SIZE * visibleRows);

        //InteractAbleWidget.drawSquareBorder(context, 0,0,1000,1000, 5, Color.RED.getRGB());
        context.enableScissor(scissorX, scissorY, scissorW, scissorH);
        //context.fill(0, 0, 2000, 2000, Color.BLUE.getRGB());
        //RenderSystem.enableScissor(509,500,510,600);
        render.run();
        context.disableScissor();
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    public int getMaxScroll() {
        return Math.max(0, contentHeight - visibleRows * SLOT_SIZE);
    }

    public float getScrollProgress() {
        int max = getMaxScroll();
        if (max == 0) return 0f;
        return (float) scrollOffset / max;
    }

    public List<DefaultInventoryScreenHandler.ManagedSlot> getActiveSlots() {
        return activeSlots;
    }

    public InventoryType getSelectedType() {
        return selectedType;
    }
}