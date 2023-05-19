package smartin.miapi.client.gui.crafting;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.ParentHandledScreen;
import smartin.miapi.client.gui.crafting.crafter.ModuleCrafter;
import smartin.miapi.client.gui.crafting.slotdisplay.SlotDisplay;
import smartin.miapi.client.gui.crafting.statdisplay.StatDisplay;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.SlotProperty;

import java.util.ArrayList;
import java.util.List;

public class CraftingGUI extends ParentHandledScreen<CraftingScreenHandler> implements ScreenHandlerProvider<CraftingScreenHandler> {

    private static final Identifier BACKGROUND_TEXTURE = new Identifier(Miapi.MOD_ID, "textures/crafting_gui_background.png");
    private ItemStack stack;
    private ModuleCrafter moduleCrafter;
    private StatDisplay statDisplay;
    private SlotDisplay slotDisplay;
    private SlotProperty.ModuleSlot baseSlot;

    public CraftingGUI(CraftingScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, Text.empty());
        this.backgroundWidth = 278;
        this.backgroundHeight = 221;
    }

    public void init() {
        List<String> allowedModules = new ArrayList<>();
        allowedModules.add("melee");
        baseSlot = new SlotProperty.ModuleSlot(allowedModules);
        moduleCrafter = new ModuleCrafter((this.width - this.backgroundWidth) / 2 + 109, (this.height - this.backgroundHeight) / 2 + 8, 163, 150, (selectedSlot) -> {
            slotDisplay.select(selectedSlot);
        }, (item) -> {
            slotDisplay.setItem(item);
            statDisplay.setCompareTo(item);
        }, handler.inventory,
                handler::addSlotByClient, handler::removeSlotByClient);
        moduleCrafter.setPacketIdentifier(handler.packetID);
        slotDisplay = new SlotDisplay(stack, (this.width - this.backgroundWidth) / 2 + 8, (this.height - this.backgroundHeight) / 2 + 8, 206 - 20, 98, (selected) -> {
            moduleCrafter.setSelectedSlot(selected);
        });
        slotDisplay.setItem(getItem());
        statDisplay = new StatDisplay((this.width - this.backgroundWidth) / 2 + 8 - 108 + 18, (this.height - this.backgroundHeight) / 2 - 1, 86, 206 - 20);
        this.addChild(statDisplay);
        this.addSelectableChild(slotDisplay);
        moduleCrafter.setItem(getItem());
        this.addSelectableChild(moduleCrafter);
        super.init();
        playerInventoryTitleX = -1000;
        playerInventoryTitleY = -1000;

    }

    public ItemStack getItem() {
        return handler.inventory.getStack(0);
    }

    public void setItem(ItemStack stack) {
        if (stack == null) {
            stack = ItemStack.EMPTY;
        }
        handler.inventory.setStack(0, stack);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.drawBackground(matrices, delta, mouseX, mouseY);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
        if (!getItem().equals(stack)) {
            stack = getItem();
            setItem(handler.inventory.getStack(0));
            baseSlot.inSlot = ItemModule.getModules(stack);
            SlotProperty.ModuleSlot current = baseSlot;
            if (baseSlot.inSlot.module.equals(ItemModule.empty)) {
                current = null;
            }
            if (slotDisplay != null) {
                slotDisplay.setBaseSlot(current);
                slotDisplay.setItem(stack);
            }
            if (moduleCrafter != null) {
                moduleCrafter.setBaseSlot(current);
                moduleCrafter.setItem(stack);
                moduleCrafter.setSelectedSlot(null);
            }
            if (statDisplay != null) {
                statDisplay.setOriginal(stack);
            }
        }
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        drawTexture(matrices, i, j, 0, 0, 0, this.backgroundWidth, this.backgroundHeight, this.backgroundWidth, this.backgroundHeight);
    }
}
