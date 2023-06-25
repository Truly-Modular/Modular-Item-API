package smartin.miapi.client.gui.crafting;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.BoxList;
import smartin.miapi.client.gui.ParentHandledScreen;
import smartin.miapi.client.gui.TransformableWidget;
import smartin.miapi.client.gui.crafting.crafter.ModuleCrafter;
import smartin.miapi.client.gui.crafting.slotdisplay.SlotDisplay;
import smartin.miapi.client.gui.crafting.statdisplay.StatDisplay;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.AllowedSlots;
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
        float scale = 0.667f;

        moduleCrafter = new ModuleCrafter((int) (((this.width - this.backgroundWidth) / 2 + 109 + 36) * (1 / scale)), (int) (((this.height - this.backgroundHeight) / 2 + 8) * (1 / scale)), (int) ((163 - 36) * (1 / scale)), (int) ((150 - 38 + 15) * (1 / scale)), (selectedSlot) -> {
            slotDisplay.select(selectedSlot);
        }, (item) -> {
            Miapi.LOGGER.error("Module Crafter Callback");
            slotDisplay.setItem(item);
            statDisplay.setCompareTo(item);
        }, handler.inventory,
                handler::addSlotByClient, handler::removeSlotByClient);
        moduleCrafter.setPacketIdentifier(handler.packetID);
        TransformableWidget transformableWidget = new TransformableWidget((this.width - this.backgroundWidth) / 2 + 109 + 36, (this.height - this.backgroundHeight) / 2 + 8, 163 - 36, 150, Text.empty());
        transformableWidget.rawProjection.loadIdentity();
        transformableWidget.rawProjection.multiply(Matrix4f.scale(scale, scale, scale));
        this.addChild(transformableWidget);
        transformableWidget.addChild(moduleCrafter);

        slotDisplay = new SlotDisplay(stack, (this.width - this.backgroundWidth) / 2 + 8, (this.height - this.backgroundHeight) / 2 + 8 + 132 - 24, 74 + 24, 98, (selected) -> {
            //moduleCrafter.setSelectedSlot(selected);
        });
        slotDisplay.setItem(getItem());
        this.addChild(slotDisplay);
        statDisplay = new StatDisplay((this.width - this.backgroundWidth) / 2 + 8 - 108 + 18 + 89, (this.height - this.backgroundHeight) / 2 - 1 + 8, 86 + 18 * 2 + 5, 206 - 100);
        this.addChild(statDisplay);

        super.init();
        playerInventoryTitleX = -1000;
        playerInventoryTitleY = -1000;
        updateItem(handler.inventory.getStack(0));
        if (moduleCrafter != null) {
            moduleCrafter.handler = handler;
        }
        this.handler.addListener(new ScreenHandlerListener() {
            @Override
            public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
                if (slotId == 36) {
                    updateItem(stack);
                }
            }

            @Override
            public void onPropertyUpdate(ScreenHandler handler, int property, int value) {

            }
        });

    }

    public ItemStack getItem() {
        return handler.inventory.getStack(0);
    }

    public void setItem(ItemStack stack) {
        if (stack == null) {
            stack = ItemStack.EMPTY;
        }
        slotDisplay.setItem(stack);
        handler.inventory.setStack(0, stack);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.drawBackground(matrices, delta, mouseX, mouseY);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
        this.renderHover(matrices, mouseX, mouseY, delta);
    }

    private void updateItem(ItemStack stack) {
        slotDisplay.setItem(stack);
        ItemStack converted = ModularItemStackConverter.getModularVersion(stack);

        setItem(handler.inventory.getStack(0));
        baseSlot.inSlot = ItemModule.getModules(converted);
        //baseSlot.allowed = AllowedSlots.getAllowedSlots(baseSlot.inSlot.module);
        SlotProperty.ModuleSlot current = baseSlot;
        if (baseSlot.inSlot.module.equals(ItemModule.empty)) {
            current = null;
        }
        if (slotDisplay != null) {
            slotDisplay.setBaseSlot(current);
            slotDisplay.select(current);
            slotDisplay.setItem(converted);
        }
        if (moduleCrafter != null) {
            moduleCrafter.setBaseSlot(current);
            moduleCrafter.setItem(converted);
            moduleCrafter.setSelectedSlot(null);
        }
        if (statDisplay != null) {
            statDisplay.setOriginal(converted);
            statDisplay.setCompareTo(converted);
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
