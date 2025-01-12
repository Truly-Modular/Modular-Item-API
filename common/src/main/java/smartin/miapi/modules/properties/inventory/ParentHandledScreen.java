package smartin.miapi.modules.properties.inventory;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class ParentHandledScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.parse("textures/gui/container/inventory.png");

    public ParentHandledScreen(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
    }
/*
    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        // Render slots above the player inventory
        if (menu instanceof CustomInventoryMenu customMenu) {
            Map<ItemInventoryManager.SlotInfo, List<InventoryUtil.InventoryType>> inventoryMap = customMenu.getInventoryMap();
            int yOffset = this.topPos + 10;
            for (Map.Entry<ItemInventoryManager.SlotInfo, List<ItemInventoryManager.InventoryType>> entry : inventoryMap.entrySet()) {
                ItemInventoryManager.SlotInfo slotInfo = entry.getKey();
                List<ItemInventoryManager.InventoryType> inventoryTypes = entry.getValue();

                // Render slot type name
                drawCenteredString(poseStack, this.font, slotInfo.getName().getString(), this.leftPos + this.imageWidth / 2, yOffset, 0xFFFFFF);
                yOffset += 10;

                // Render slots color-coded by slotInfo color
                for (ItemInventoryManager.InventoryType inventoryType : inventoryTypes) {
                    int color = slotInfo.getColor().getValue();
                    fill(poseStack, this.leftPos + 10, yOffset, this.leftPos + 26, yOffset + 16, color);

                    // Render slot icon
                    slotInfo.renderIcon(this.leftPos + 12, yOffset + 2, 12, 12);

                    yOffset += 20;
                }
            }
        }
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        super.renderLabels(poseStack, mouseX, mouseY);
        this.font.draw(poseStack, "Inventory", 8, this.imageHeight - 96 + 2, 0x404040);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle clicking on slots or other interactions
        return super.mouseClicked(mouseX, mouseY, button);
    }
    #
 */
}