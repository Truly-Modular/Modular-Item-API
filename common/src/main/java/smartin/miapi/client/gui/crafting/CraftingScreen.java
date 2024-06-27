package smartin.miapi.client.gui.crafting;

import com.mojang.blaze3d.systems.RenderSystem;
import net.bettercombat.logic.WeaponAttributesFallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ParentHandledScreen;
import smartin.miapi.client.gui.SimpleScreenHandlerListener;
import smartin.miapi.client.gui.TransformableWidget;
import smartin.miapi.client.gui.crafting.crafter.DetailView;
import smartin.miapi.client.gui.crafting.crafter.ModuleCrafter;
import smartin.miapi.client.gui.crafting.slotdisplay.SlotDisplay;
import smartin.miapi.client.gui.crafting.slotdisplay.SmithDisplay;
import smartin.miapi.client.gui.crafting.statdisplay.material.MaterialStatWidget;
import smartin.miapi.client.gui.crafting.statdisplay.material.StatDisplayWidget;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.modules.edit_options.EditOptionIcon;
import smartin.miapi.modules.properties.AllowedSlots;
import smartin.miapi.modules.properties.SlotProperty;
import smartin.miapi.registries.RegistryInventory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class CraftingScreen extends ParentHandledScreen<CraftingScreenHandler> implements MenuAccess<CraftingScreenHandler> {
    public static final ResourceLocation BACKGROUND_TEXTURE = Miapi.id("textures/gui/crafter/background.png");
    /*public static final InterpolateMode EASE_IN = new InterpolateMode.EaseIn(5);
    public static final InterpolateMode EASE_OUT = new InterpolateMode.EaseOut(5);*/
    private ItemStack stack;
    private ModuleCrafter moduleCrafter;
    private StatDisplayWidget statDisplay;
    private MaterialStatWidget materialStatWidget;
    private SlotDisplay slotDisplay;
    private SmithDisplay smithDisplay;
    private MinimizeButton minimizer;
    private SlotProperty.ModuleSlot baseSlot;
    @Nullable
    public static SlotProperty.ModuleSlot slot;
    @Nullable
    public static EditOption editOption;
    private TransformableWidget editHolder;
    static int editSpace = 30;
    @Nullable
    public InteractAbleWidget hoverElement = null;
    static WeakReference<CraftingScreen> craftingScreenWeakReference = new WeakReference<>(null);

    List<InteractAbleWidget> editOptionIcons = new ArrayList<>();

    public CraftingScreen(CraftingScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, Component.empty());
        slot = null;
        this.imageWidth = 369 + 12 - 15 + 6;

        this.imageHeight = 223 - 9 - 15;

        DetailView.scrollPos = 0;
        craftingScreenWeakReference = new WeakReference<>(this);
    }

    @Nullable
    public static CraftingScreen getInstance() {
        if (craftingScreenWeakReference != null && craftingScreenWeakReference.get() != null) {
            return craftingScreenWeakReference.get();
        }
        WeaponAttributesFallback fallback;
        return null;
    }

    public EditOption getEditOption() {
        return editOption;
    }

    public void selectSlot(SlotProperty.ModuleSlot slot) {
        this.slot = slot;
        updateEditOptions();
    }

    public void previewStack(ItemStack itemStack) {
        slotDisplay.setItem(itemStack);
        statDisplay.setCompareTo(itemStack);
        smithDisplay.setPreview(itemStack);
    }

    public void selectEditOption(EditOption editOption) {
        this.editOption = editOption;
        moduleCrafter.setSelectedSlot(slot);
        moduleCrafter.setEditMode(editOption, get(editOption));
        PreviewManager.resetCursorStack();
    }

    public void init() {
        slot = null;
        editOption = null;
        List<String> allowedModules = new ArrayList<>();
        allowedModules.add("melee");
        baseSlot = new SlotProperty.ModuleSlot(allowedModules);

        int centerX = (this.width - this.imageWidth - 6) / 2;
        int centerY = (this.height - this.imageHeight) / 2;

        moduleCrafter = new ModuleCrafter(centerX + 51 - 15, centerY + 22 - 14, 144, 89, this::selectSlot, (item) -> {
            slotDisplay.setItem(item);
            statDisplay.setCompareTo(item);
            smithDisplay.setPreview(item);
        }, menu.inventory,
                menu::addSlotByClient, menu::removeSlotByClient);
        moduleCrafter.setPacketIdentifier(menu.packetID);
        this.addChild(moduleCrafter);

        slotDisplay = new SlotDisplay(ItemStack.EMPTY, centerX + 51 - 15, centerY + 117 - 14, 68, 87, (selected) -> {

        });
        slotDisplay.setItem(getItem());
        this.addChild(slotDisplay);
        smithDisplay = new SmithDisplay(centerX + 140 - 15, centerY + 117 - 14, 55, 70);
        this.addChild(smithDisplay);
        statDisplay = new StatDisplayWidget(centerX + 213 - 15, centerY + 30 - 14, 161, 95);
        this.addChild(statDisplay);

        minimizer = new MinimizeButton(centerX + 178 - 15, centerY + 188 - 14, 18, 18, this::minimizeView, this::maximizeView);
        this.addChild(minimizer);

        super.init();
        inventoryLabelX = -1000;
        inventoryLabelY = -1000;

        editHolder = new TransformableWidget(leftPos - 15, topPos - 14, 0, 0, Component.empty());
        addChild(editHolder);

        updateItem(menu.inventory.getItem(0));
        if (moduleCrafter != null) {
            moduleCrafter.handler = menu;
        }
        this.menu.addSlotListener(new SimpleScreenHandlerListener((handler, slotId, stack) -> {
            if (slotId == 36) {
                updateItem(stack);
            }
        }));

        addChild(new EditOptionIcon(moduleCrafter.getX() - 36, moduleCrafter.getY() + 4, 32, 28, this::selectEditOption, this::getEditOption, BACKGROUND_TEXTURE, 339, 25, 512, 512, "miapi.ui.edit_option.hover.info", null));
        selectEditOption(null);
        selectSlot(null);
        previewStack(menu.inventory.getItem(0));
        PreviewManager.resetCursorStack();
        PreviewManager.resetPreview();
    }

    public int getBackgroundHeight() {
        return this.imageHeight;
    }

    public int getBackgroundWidth() {
        return this.imageWidth;
    }

    //could be the same as maximizeView()
    public void minimizeView() {
        EditOption op = getEditOption();
        removeWidget(moduleCrafter);
        SlotProperty.ModuleSlot slot1 = moduleCrafter.slot;
        editHolder.children().clear();
        moduleCrafter = new ModuleCrafter(moduleCrafter.getX(), moduleCrafter.getY(), moduleCrafter.getWidth(), moduleCrafter.getHeight() + 74, moduleCrafter);
        moduleCrafter.handler = menu;
        moduleCrafter.setPacketIdentifier(menu.packetID);
        addChild(moduleCrafter);
        updateItem(getItem());
        moduleCrafter.setSelectedSlot(slot1);
        updateEditOptions();
        removeWidget(slotDisplay);
        removeWidget(smithDisplay);
        selectSlot(slot1);
        updateEditOptions();
        selectEditOption(op);
    }

    public void maximizeView() {
        EditOption op = getEditOption();
        removeWidget(moduleCrafter);
        SlotProperty.ModuleSlot slot1 = moduleCrafter.slot;
        editHolder.children().clear();
        moduleCrafter = new ModuleCrafter(moduleCrafter.getX(), moduleCrafter.getY(), moduleCrafter.getWidth(), moduleCrafter.getHeight() - 74, moduleCrafter);
        moduleCrafter.handler = menu;
        moduleCrafter.setPacketIdentifier(menu.packetID);
        addChild(moduleCrafter);
        updateItem(getItem());
        moduleCrafter.setSelectedSlot(slot1);
        updateEditOptions();
        addChild(slotDisplay);
        addChild(smithDisplay);
        selectSlot(slot1);
        updateEditOptions();
        selectEditOption(op);
    }

    public ItemStack getItem() {
        return menu.inventory.getItem(0);
    }

    public void setItem(ItemStack stack) {
        if (stack == null) {
            stack = ItemStack.EMPTY;
        }
        slotDisplay.setItem(stack);
        menu.inventory.setItem(0, stack);
    }

    public void updateItem(ItemStack stack) {
        PreviewManager.resetCursorStack();
        updatePreviewItemStack(stack);
    }

    public void updatePreviewItemStack(ItemStack stack) {
        stack = stack.copy();
        slotDisplay.setItem(stack);
        ItemStack converted = ModularItemStackConverter.getModularVersion(stack).copy();
        baseSlot.inSlot = ItemModule.getModules(converted);
        baseSlot.allowed = AllowedSlots.getAllowedSlots(baseSlot.inSlot.module);
        SlotProperty.ModuleSlot current = baseSlot;
        slot = null;
        if (baseSlot.inSlot.module.equals(ItemModule.empty)) {
            current = null;
        }
        if (moduleCrafter != null) {
            moduleCrafter.setItem(converted);
            moduleCrafter.setBaseSlot(current);
            moduleCrafter.setSelectedSlot(null);
            moduleCrafter.setItem(converted);
            moduleCrafter.setBaseSlot(current);
            moduleCrafter.setSelectedSlot(null);
        }
        if (slotDisplay != null) {
            slotDisplay.setItem(converted);
            slotDisplay.select(current);
        }
        if (smithDisplay != null) {
            smithDisplay.setPreview(converted);
        }
        if (statDisplay != null) {
            statDisplay.setItemsOriginal(converted, converted);
        }
        /*
        List<Integer> slotPos = new ArrayList<>();
        if (slot != null) {
            if (slot.inSlot != null) {
                slot.inSlot.calculatePosition(slotPos);
            } else if (slot.parent != null) {
                slot.parent.getPosition(slotPos);
            }
        }
        if (baseSlot.inSlot != null) {
            //slot = SlotProperty.getSlotIn(baseSlot.inSlot.getRoot().getPosition(slotPos));
        } else {
            slot = null;
        }
         */
        updateEditOptions();
    }

    public void updateEditOptions() {
        int x = moduleCrafter.getX() - 36;
        int y = moduleCrafter.getY() + 4 + editSpace;

        for (InteractAbleWidget widget : editOptionIcons) {
            editHolder.removeChild(widget);
        }
        editHolder.children().clear();

        for (EditOption option : RegistryInventory.editOptions.getFlatMap().values()) {
            EditOption.EditContext context = get(option);
            if (option.isVisible(context)) {
                InteractAbleWidget widget = option.getIconGui(x, y, 32, 28, this::selectEditOption, this::getEditOption);
                y += editSpace;
                editHolder.addChild(widget);
                editOptionIcons.add(widget);
            }
        }
        if (!editHolder.children().contains(editOption)) {
            editOption = null;
        }
        PreviewManager.resetCursorStack();
        selectEditOption(editOption);
    }

    public EditOption.EditContext get(EditOption editOption) {
        return new EditOption.EditContext() {
            @Override
            public void craft(FriendlyByteBuf craftBuffer) {
                if (editOption != null) {
                    editOption.execute(craftBuffer, this);
                }
            }

            @Override
            public void preview(FriendlyByteBuf preview) {
                if (editOption != null) {

                    previewStack(editOption.preview(preview, this));
                }
            }

            @Override
            public SlotProperty.ModuleSlot getSlot() {
                return slot;
            }

            @Override
            public ItemStack getItemstack() {
                return moduleCrafter.stack;
            }

            @Override
            public @Nullable ModuleInstance getInstance() {
                if (getSlot() == null) {
                    return null;
                }
                return getSlot().inSlot;
            }

            @Override
            public @Nullable Player getPlayer() {
                return Minecraft.getInstance().player;
            }

            @Override
            public @Nullable ModularWorkBenchEntity getWorkbench() {
                if (Minecraft.getInstance().player.containerMenu instanceof CraftingScreenHandler craftingScreenHandler) {
                    return craftingScreenHandler.blockEntity;
                }
                return null;
            }

            @Override
            public Container getLinkedInventory() {
                if (Minecraft.getInstance().player.containerMenu instanceof CraftingScreenHandler craftingScreenHandler) {
                    return craftingScreenHandler.inventory;
                }
                return null;
            }

            @Override
            public CraftingScreenHandler getScreenHandler() {
                if (Minecraft.getInstance().player.containerMenu instanceof CraftingScreenHandler craftingScreenHandler) {
                    return craftingScreenHandler;
                }
                return null;
            }
        };
    }

    @Override
    public void render(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        renderBackground(drawContext, mouseX, mouseY, delta);
        int i = (this.width - this.imageWidth - 6) / 2;
        int j = (this.height - this.imageHeight) / 2;
        //InteractAbleWidget.drawSquareBorder(drawContext, i, j, this.backgroundWidth, this.backgroundHeight, 1, ColorHelper.Argb.getArgb(255, 255, 0, 0));
        //(Identifier texture, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight)
        drawContext.blit(BACKGROUND_TEXTURE, i + 43 - 15, j + 14 - 14, 338, 199, 0.0f, 0.0f, 338, 199, 512, 512);

        // long timeSinceMod = Util.getMeasuringTimeMs()-minimizer.getLastChangeTime();
        if (minimizer.isEnabled()/* || timeSinceMod < 1000*/) {
            /*float progress = MathHelper.clamp(timeSinceMod/1000f, 0, 1);
            int start;
            int end;
            InterpolateMode interp;
            if (minimizer.isEnabled()) {
                start = 206;
                end = 111;
                interp = EASE_OUT;
            } else {
                start = 111;
                end = 206;
                interp = EASE_IN;
            }

            int pos = (int) interp.interpolate(progress, start, end);
            boolean disableScissor = minimizer.isEnabled() ? pos > end : pos < end;
            if (disableScissor) drawContext.enableScissor(i+42, j+110, i+204, j+187);
            drawContext.drawTexture(BACKGROUND_TEXTURE, i + 43, j + pos, 160, 95, 0, 199, 160, 95, 512, 512);
            if (disableScissor) drawContext.disableScissor();
            //InteractAbleWidget.drawSquareBorder(drawContext, i + 43, j + 111, 160, 77, 1, Color.RED.argb());*/

            drawContext.blit(BACKGROUND_TEXTURE, i + 43 - 15, j + 111 - 14, 160, 95, 0, 199, 160, 95, 512, 512);
        }
        if (hoverElement == null) {
            super.render(drawContext, mouseX, mouseY, delta);
        } else {
            hoverElement.render(drawContext, mouseX, mouseY, delta);
        }
        this.renderTooltip(drawContext, mouseX, mouseY);
        drawContext.pose().pushPose();
        drawContext.pose().translate(0.0F, 0.0F, 400.0F);
        if (hoverElement == null) {
            drawContext.drawManaged(() -> {
                this.renderHover(drawContext, mouseX, mouseY, delta);
            });
        } else {
            drawContext.drawManaged(() -> {
                hoverElement.renderHover(drawContext, mouseX, mouseY, delta);
            });
        }
        drawContext.pose().popPose();
        PreviewManager.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hoverElement != null) {
            return hoverElement.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (hoverElement != null) {
            hoverElement.mouseMoved(mouseX, mouseY);
        } else {
            super.mouseMoved(mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (hoverElement != null) {
            return hoverElement.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (hoverElement != null) {
            return hoverElement.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount, double other) {
        if (hoverElement != null) {
            return hoverElement.mouseScrolled(mouseX, mouseY, amount, other);
        }
        return super.mouseScrolled(mouseX, mouseY, amount, other);
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
    }
}
