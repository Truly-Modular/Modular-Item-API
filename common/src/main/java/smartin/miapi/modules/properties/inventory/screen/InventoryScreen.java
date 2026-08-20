package smartin.miapi.modules.properties.inventory.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.ParentHandledScreen;
import smartin.miapi.modules.properties.inventory.SlotInfo;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public class InventoryScreen extends ParentHandledScreen<DefaultInventoryScreenHandler> {
    public static final ResourceLocation BACKGROUND_TEXTURE = Miapi.id("textures/gui/backpack/background.png");

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
        layoutManager.scroll((int) -(scrollY * 3));
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
        int left = this.leftPos;
        int top = this.topPos;
        layoutManager.withScissor(gfx, () -> {
            for (DefaultInventoryScreenHandler.ManagedInventory managed : menu.getManagedInventories()) {
                for (int i = managed.firstSlot; i <= managed.lastSlot; i++) {
                    DefaultInventoryScreenHandler.ManagedSlot slot = menu.getManagedSlots().get(i);
                    if (!slot.isActive()) continue;

                    if (hoveredInventory != null && !hoveredInventory.equals(managed.slotInfo)) {
                        gfx.blit(BACKGROUND_TEXTURE,
                                left + slot.x - 1,
                                top + slot.y - 1,
                                18, 18,
                                176.0f, 18f,
                                18, 18,
                                512, 512);

                    } else {
                        gfx.blit(BACKGROUND_TEXTURE,
                                left + slot.x - 1,
                                top + slot.y - 1,
                                18, 18,
                                176.0f, 0f,
                                18, 18,
                                512, 512);
                    }
                }
            }
        });
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

            yOffset = renderBackpackSourceLeft(context, mouseX, mouseY, info, left, height, top, yOffset);
        }

        // Apply filtering to layout manager
        if (hoveredInventory != null) {
            //layoutManager.setSelectedType(hoveredInventory);
        } else {
            //layoutManager.clearSelectedType();
        }
    }

    /**
     * renders the items and handles the hover logic for the items on the left
     */
    private int renderBackpackSourceLeft(GuiGraphics context, int mouseX, int mouseY, SlotInfo info, int left, int height, int top, int yOffset) {
        int x1 = left;
        int x2 = left + height + 4;
        int y1 = top + yOffset;
        int y2 = y1 + height;

        ItemStack stack = info.getStack(minecraft.player);
        context.renderItem(stack, x1, y1);

        if (mouseX >= x1 && mouseX <= x2 &&
            mouseY >= y1 && mouseY <= y2) {
            hoveredInventory = info;
            context.renderTooltip(Minecraft.getInstance().font, info.getName(), mouseX, mouseY);
        }

        yOffset += height;
        return yOffset;
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float delta, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth - 6) / 2 + leftSlotInfSpace - 5;
        int j = (this.height - this.imageHeight) / 2 - 2;
        gfx.blit(BACKGROUND_TEXTURE, i, j, 175, 3, 0.0f, 0.0f, 175, 3, 512, 512);
        int slotHeight = 6 * 18;
        gfx.blit(BACKGROUND_TEXTURE, i, j + 3, 175, slotHeight + 15, 0.0f, 3.0f, 175, 12, 512, 512);
        gfx.blit(BACKGROUND_TEXTURE, i, j + slotHeight + 15, 175, 84, 0.0f, 15.0f, 175, 84, 512, 512);
        renderHighlights(gfx);
    }
}