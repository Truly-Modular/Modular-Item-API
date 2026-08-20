package smartin.miapi.modules.properties.inventory.screen;

import net.minecraft.client.gui.GuiGraphics;
import smartin.miapi.modules.properties.inventory.InventoryType;

import java.util.*;

public class SlotLayoutManager {

    private final List<DefaultInventoryScreenHandler.ManagedSlot> allSlots;
    private final List<InventorySection> sections;

    private final List<DefaultInventoryScreenHandler.ManagedSlot> activeSlots =
            new ArrayList<>();

    private final int visibleRows;
    private final int visibleHeight;

    private final int startX;
    private final int startY;

    private final int textX;
    private final int textY;

    private final LinkedHashMap<InventorySection, Integer> sectionPositions =
            new LinkedHashMap<>();

    private InventoryType selectedType;

    private int scrollOffset;
    private int contentHeight;

    public SlotLayoutManager(
            List<DefaultInventoryScreenHandler.ManagedInventory> inventories,
            List<DefaultInventoryScreenHandler.ManagedSlot> slots,
            int visibleRows,
            int x,
            int y,
            int topX,
            int topY
    ) {
        this.allSlots = slots;

        this.visibleRows = visibleRows;
        this.visibleHeight = visibleRows * InventorySection.SLOT_SIZE;

        this.startX = x;
        this.startY = y;

        this.textX = topX;
        this.textY = topY;

        this.sections = createSections(inventories, slots);

        layout(true);
    }

    private List<InventorySection> createSections(
            List<DefaultInventoryScreenHandler.ManagedInventory> inventories,
            List<DefaultInventoryScreenHandler.ManagedSlot> slots
    ) {
        Map<InventoryType, List<DefaultInventoryScreenHandler.ManagedInventory>> grouped =
                new LinkedHashMap<>();

        inventories.stream()
                .sorted(Comparator.comparingDouble(
                        inventory -> inventory.type.priority()
                ))
                .forEach(inventory ->
                        grouped
                                .computeIfAbsent(inventory.type, ignored -> new ArrayList<>())
                                .add(inventory)
                );

        return grouped.entrySet()
                .stream()
                .map(entry -> new InventorySection(
                        entry.getKey(),
                        entry.getValue(),
                        slots
                ))
                .toList();
    }

    public void setSelectedType(InventoryType type) {
        selectedType = type;
        scrollOffset = 0;
        layout(true);
    }

    public void clearSelectedType() {
        selectedType = null;
        scrollOffset = 0;
        layout(true);
    }

    public void scroll(int deltaPixels) {
        scrollOffset += deltaPixels;
        clampScroll();
        layout(true);
    }

    public void setScroll(int value) {
        scrollOffset = value;
        clampScroll();
        layout(true);
    }

    public void update(
            GuiGraphics context,
            InventoryScreen screen,
            int mouseX,
            int mouseY
    ) {
        boolean isMouseOver = isMouseOver(mouseX, mouseY);

        if (screen.getFocusSlot()
                    instanceof DefaultInventoryScreenHandler.ManagedSlot
            && !isMouseOver) {
            screen.resetHoverSlot();
        }

        activeSlots.forEach(slot ->
                slot.setHighlightable(isMouseOver)
        );
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        int left = textX + startX;
        int top = textY;
        int right = left + InventorySection.SLOT_SIZE * InventorySection.COLUMNS;
        int bottom = top + visibleHeight;

        return mouseX > left
               && mouseX < right
               && mouseY > top
               && mouseY < bottom;
    }

    public void layout(boolean applyPositions) {
        if (applyPositions) {
            for (DefaultInventoryScreenHandler.ManagedSlot slot : allSlots) {
                slot.setEnabled(false);
            }
        }

        activeSlots.clear();
        sectionPositions.clear();

        int yCursor = 0;

        for (InventorySection section : sections) {
            if (selectedType != null &&
                section.getType() != selectedType) {
                //continue;
            }

            sectionPositions.put(section, yCursor);

            section.layout(
                    startX,
                    yCursor,
                    scrollOffset,
                    visibleHeight
            );

            activeSlots.addAll(getSectionSlots(section));

            yCursor += section.getRequiredHeight();
        }

        contentHeight = yCursor;
        clampScroll();
    }

    private List<DefaultInventoryScreenHandler.ManagedSlot> getSectionSlots(
            InventorySection section
    ) {
        List<DefaultInventoryScreenHandler.ManagedSlot> result = new ArrayList<>();

        for (DefaultInventoryScreenHandler.ManagedInventory inventory :
                section.getInventories()) {

            for (int i = inventory.firstSlot; i <= inventory.lastSlot; i++) {
                result.add(allSlots.get(i));
            }
        }

        return result;
    }

    public void renderHeaders(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        if (selectedType != null) {
            return;
        }

        withScissor(graphics, () -> {
            for (var entry : sectionPositions.entrySet()) {
                InventorySection section = entry.getKey();
                int sectionY = entry.getValue();

                section.renderHeader(
                        graphics,
                        startX + textX,
                        startY + sectionY - scrollOffset,
                        0,
                        visibleHeight
                );
            }
        });
    }

    public void withScissor(
            GuiGraphics graphics,
            Runnable render
    ) {
        int left = textX + startX - 1;
        int top = textY;
        int right = left + InventorySection.SLOT_SIZE * InventorySection.COLUMNS;
        int bottom = top + visibleHeight;

        graphics.enableScissor(left, top, right, bottom);

        try {
            render.run();
        } finally {
            graphics.disableScissor();
        }
    }

    private void clampScroll() {
        int maxScroll = getMaxScroll();

        if (scrollOffset < 0) {
            scrollOffset = 0;
        }

        if (scrollOffset > maxScroll) {
            scrollOffset = maxScroll;
        }
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    public int getMaxScroll() {
        return Math.max(0, contentHeight - visibleHeight);
    }

    public float getScrollProgress() {
        int max = getMaxScroll();

        if (max == 0) {
            return 0.0f;
        }

        return (float) scrollOffset / max;
    }

    public int getContentHeight() {
        return contentHeight;
    }

    public int getVisibleHeight() {
        return visibleHeight;
    }

    public int getVisibleRows() {
        return visibleRows;
    }

    public List<DefaultInventoryScreenHandler.ManagedSlot> getActiveSlots() {
        return activeSlots;
    }

    public InventoryType getSelectedType() {
        return selectedType;
    }

    public List<InventorySection> getSections() {
        return sections;
    }
}