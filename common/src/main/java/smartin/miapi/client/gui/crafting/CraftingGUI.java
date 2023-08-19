package smartin.miapi.client.gui.crafting;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.ParentHandledScreen;
import smartin.miapi.client.gui.SimpleScreenHandlerListener;
import smartin.miapi.client.gui.TransformableWidget;
import smartin.miapi.client.gui.crafting.crafter.ModuleCrafter;
import smartin.miapi.client.gui.crafting.slotdisplay.SlotDisplay;
import smartin.miapi.client.gui.crafting.statdisplay.StatDisplay;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.SlotProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * The main Modular Work Benchs Screen
 */
@Environment(EnvType.CLIENT)
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
        this.backgroundHeight = 222;
    }

    public void init() {
        List<String> allowedModules = new ArrayList<>();
        allowedModules.add("melee");
        baseSlot = new SlotProperty.ModuleSlot(allowedModules);
        float scale = 0.667f;

        moduleCrafter = new ModuleCrafter((int) (((this.width - this.backgroundWidth) / 2 + 109 + 36) * (1 / scale)), (int) (((this.height - this.backgroundHeight) / 2 + 8) * (1 / scale)), (int) ((163 - 36) * (1 / scale)), (int) ((150 - 38 + 15) * (1 / scale)), (selectedSlot) -> {
            slotDisplay.select(selectedSlot);
        }, (item) -> {
            slotDisplay.setItem(item);
            statDisplay.setCompareTo(item);
        }, handler.inventory,
                handler::addSlotByClient, handler::removeSlotByClient);
        moduleCrafter.setPacketIdentifier(handler.packetID);
        TransformableWidget transformableWidget = new TransformableWidget((this.width - this.backgroundWidth) / 2 + 109 + 36, (this.height - this.backgroundHeight) / 2 + 8, 163 - 36, 150, Text.empty());
        transformableWidget.rawProjection = new Matrix4f();
        transformableWidget.rawProjection.scale(scale);
        this.addChild(transformableWidget);
        transformableWidget.addChild(moduleCrafter);

        slotDisplay = new SlotDisplay(stack, (this.width - this.backgroundWidth) / 2 + 8, (this.height - this.backgroundHeight) / 2 + 8 + 132 - 24, 74 + 24, 98, (selected) -> {

        });
        slotDisplay.setItem(getItem());
        this.addChild(slotDisplay);
        statDisplay = new StatDisplay((this.width - this.backgroundWidth) / 2 + 8 - 108 + 18 + 89, (this.height - this.backgroundHeight) / 2 - 1 + 8, 86 + 18 * 2 + 5, 95);
        this.addChild(statDisplay);

        super.init();
        playerInventoryTitleX = -1000;
        playerInventoryTitleY = -1000;
        updateItem(handler.inventory.getStack(0));
        if (moduleCrafter != null) {
            moduleCrafter.handler = handler;
        }
        this.handler.addListener(new SimpleScreenHandlerListener((handler, slotId, stack) -> {
            if (slotId == 36) {
                updateItem(stack);
            }
        }));

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
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.drawBackground(drawContext, delta, mouseX, mouseY);
        super.render(drawContext, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(drawContext, mouseX, mouseY);
        this.renderHover(drawContext, mouseX, mouseY, delta);
    }

    private void updateItem(ItemStack stack) {
        slotDisplay.setItem(stack);
        ItemStack converted = ModularItemStackConverter.getModularVersion(stack);

        baseSlot.inSlot = ItemModule.getModules(converted);
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
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(BACKGROUND_TEXTURE, i, j, 0, 0, 0, this.backgroundWidth, this.backgroundHeight, this.backgroundWidth, this.backgroundHeight);
    }
}
