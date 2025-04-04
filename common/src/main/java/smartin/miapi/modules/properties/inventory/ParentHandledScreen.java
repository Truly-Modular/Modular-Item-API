package smartin.miapi.modules.properties.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.List;
import java.util.Map;

public abstract class ParentHandledScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.parse("textures/gui/container/generic_54.png");
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_SPACING = 4;
    private static final int TITLE_SPACING = 10;

    public ParentHandledScreen(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.renderTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        // Draw the background
        context.blit(BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        if (menu instanceof CustomInventoryMenu customMenu) {
            Map<ItemInventoryManager.SlotInfo, List<ItemInventoryManager.InventoryType>> inventoryMap = customMenu.getInventoryMap();
            int currentY = this.topPos + TITLE_SPACING;

            for (Map.Entry<ItemInventoryManager.SlotInfo, List<ItemInventoryManager.InventoryType>> entry : inventoryMap.entrySet()) {
                ItemInventoryManager.SlotInfo slotInfo = entry.getKey();
                List<ItemInventoryManager.InventoryType> inventoryTypes = entry.getValue();

                // Draw slot title
                context.drawString(this.font, slotInfo.getName().getString(), 
                    this.leftPos + 8, currentY, 0x404040, false);
                currentY += TITLE_SPACING;

                // Draw slots for each inventory type
                for (ItemInventoryManager.InventoryType inventoryType : inventoryTypes) {
                    int color = slotInfo.getColor().argb();
                    int slots = inventoryType.getSize();
                    int rows = (int) Math.ceil(slots / 9.0);
                    
                    // Draw colored background for slots
                    for (int row = 0; row < rows; row++) {
                        for (int col = 0; col < Math.min(9, slots - row * 9); col++) {
                            int x = this.leftPos + 8 + col * (SLOT_SIZE + SLOT_SPACING);
                            int y = currentY + row * (SLOT_SIZE + SLOT_SPACING);
                            
                            // Draw colored slot background
                            context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, color);
                            
                            // Draw slot border
                            context.blit(BACKGROUND_TEXTURE, x, y, 0, 0, SLOT_SIZE, SLOT_SIZE);
                        }
                    }
                    
                    currentY += rows * (SLOT_SIZE + SLOT_SPACING) + TITLE_SPACING;
                }
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics context, int mouseX, int mouseY) {
        context.drawString(this.font, this.title, 8, 6, 0x404040, false);
        context.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 0x404040, false);
    }
}