package smartin.miapi.client.gui.crafting.crafter.replace;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.*;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.modules.properties.slot.SlotProperty;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.network.Networking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * This Widget manages the actual craftView.
 * Its manages the replacing of a module with a new Module
 */
@Environment(EnvType.CLIENT)
public class CraftViewRework extends InteractAbleWidget {
    public List<Slot> currentSlots = new ArrayList<>();
    private int currentGuiIndex = 0;
    int backgroundWidth = 278;
    int backgroundHeight = 221;
    List<Component> warnings = new ArrayList<>();
    boolean firstRender = true;
    boolean isClosed = false;
    EditOption.EditContext editContext;
    CraftAction action;
    List<CraftingProperty> craftingProperties = new ArrayList<>();
    List<InteractAbleWidget> craftingGuis = new ArrayList<>();
    SimpleButton<Object> previousButton;
    SimpleButton<Object> nextButton;
    CraftButton<Object> craftButton;
    Matrix4f currentMatrix = new Matrix4f();
    public Map<String, String> defaultMap = new HashMap<>();
    static ContainerListener listener;
    EmptyCraftingWidget fallbackCraftingWidget;

    public CraftViewRework(int x, int y, int width, int height, int offset, CraftOption option, EditOption.EditContext editContext, Consumer<SlotProperty.ModuleSlot> back) {
        super(x, y, width, height, Component.empty());
        this.editContext = editContext;
        if (listener != null) {
            editContext.getScreenHandler().removeSlotListener(listener);
        }
        listener = new SimpleScreenHandlerListener((h, slotId, itemStack) -> {
            if (slotId != 36) {
                update();
            }
        });
        defaultMap = option.data();
        action = new CraftAction(editContext.getItemstack(), editContext.getSlot(), option.module(), editContext.getPlayer(), editContext.getWorkbench(), option.data());
        action.setItem(editContext.getItemstack());
        action.linkInventory(editContext.getLinkedInventory(), offset);
        setBuffers();
        ItemStack test = action.getPreview();
        action.forEachCraftingProperty(test, ((craftingProperty, moduleInstance, itemStacks, invStart, invEnd, buf) -> {
            InteractAbleWidget guiScreen = craftingProperty.createGui(this.getX(), this.getY(), this.width, this.height - 30, action);
            if (guiScreen != null) {
                craftingGuis.add(guiScreen);
                craftingProperties.add(craftingProperty);
            }
        }));
        fallbackCraftingWidget = new EmptyCraftingWidget(this.getX(), this.getY(), this.width, this.height, action);
        addChild(new SimpleButton<>(this.getX() + 2, this.getY() + this.height - 14, 40, 12, Component.translatable(Miapi.MOD_ID + ".ui.back"), null, (moduleSlot) -> {
            isClosed = true;
            closeSlot();
            back.accept(null);
        }));

        if (craftingGuis.size() > 1) {
            previousButton = new PageButton<>(this.getX() + this.width - 10, this.getY(), 10, 12, true, null, (callback) -> {
                if (currentGuiIndex > 0) {
                    removeChild(craftingGuis.get(currentGuiIndex));
                    currentGuiIndex--;
                    addGui(craftingGuis.get(currentGuiIndex));
                    if (currentGuiIndex == 0) {
                        previousButton.isEnabled = false;
                    }
                    nextButton.isEnabled = true;
                }
            });
            nextButton = new PageButton<>(this.getX() + 10, this.getY(), 10, 12, false, null, (callback) -> {
                if (currentGuiIndex < craftingGuis.size() - 1) {
                    removeChild(craftingGuis.get(currentGuiIndex));
                    currentGuiIndex++;
                    addGui(craftingGuis.get(currentGuiIndex));
                    if (currentGuiIndex == craftingGuis.size() - 1) {
                        nextButton.isEnabled = false;
                    }
                    previousButton.isEnabled = true;
                }
            });
        }

        craftButton = new CraftButton<>(this.getX() + this.width - 42, this.getY() + this.height - 14, 40, 12, Component.translatable(Miapi.MOD_ID + ".ui.craft"), null, (callback) -> {
            setBuffers();
            if (action.canPerform()) {
                isClosed = true;
                ItemStack craftedStack = action.getPreview();
                if (!ItemStack.matches(editContext.getItemstack(), craftedStack)) {
                    editContext.craft(action.toPacket(Networking.createBuffer()));
                }
                editContext.getScreenHandler().removeSlotListener(listener);
            }
        });
        addChild(craftButton);

        editContext.getScreenHandler().addSlotListener(listener);
    }

    private void update() {
        try {
            if (!isClosed) {
                ItemStack previewStack = action.getPreview();
                setBuffers();
                editContext.preview(action.toPacket(Networking.createBuffer()));
                Pair<Map<CraftingProperty, Boolean>, Boolean> canPerform = action.fullCanPerform();
                craftButton.isEnabled = canPerform.getSecond();

                warnings.clear();
                ItemStack crafted = editContext.getItemstack();
                if (ItemStack.matches(previewStack, crafted)) {
                    warnings.add(Component.translatable(Miapi.MOD_ID + ".ui.craft.result_equal_warning"));
                    craftButton.isEnabled = false;
                }
                if (previewStack.getDamageValue() > previewStack.getMaxDamage()) {
                    warnings.add(Component.translatable(Miapi.MOD_ID + ".ui.craft.warning.durability_negative"));
                    craftButton.isEnabled = false;
                }
                canPerform.getFirst().forEach((property, result) -> {
                    if (!result) {
                        Component warning = property.getWarning();
                        if (warning != null && !warning.getString().isEmpty())
                            warnings.add(warning);
                    }
                });
            }
        } catch (Exception e) {
            Miapi.LOGGER.error("surpressed", e);
        }
    }

    public void closeSlot() {
        currentSlots.forEach(slot -> {
            if (slot instanceof MutableSlot mutableSlot) {
                mutableSlot.setEnabled(true);
            }
            editContext.removeSlot(slot);
        });
    }

    public void addGui(InteractAbleWidget widget) {
        if (children.contains(widget)) {
            return;
        }
        widget.setX(this.getX());
        widget.setY(this.getY());
        widget.setWidth(this.width);
        widget.setHeight(this.height);
        currentSlots.forEach(slot -> {
            if (slot instanceof MutableSlot mutableSlot) {
                mutableSlot.setEnabled(false);
            }
        });
        CraftingProperty property = craftingProperties.get(craftingGuis.indexOf(widget));
        action.forEachCraftingProperty(action.getPreview(), (craftingProperty, module, inventory, start, end, buffer) -> {
            if (craftingProperty.equals(property)) {
                AtomicInteger counter = new AtomicInteger(0);
                property.getSlotPositions().forEach(vec2f -> {
                    int guiX = widget.getX() + (int) vec2f.x;
                    int guiY = widget.getY() + (int) vec2f.y;
                    Matrix4f inverse = new Matrix4f(currentMatrix);
                    Vector4f vector4f = TransformableWidget.transFormMousePos(guiX, guiY, inverse);
                    guiX = (int) (vector4f.x() - (Minecraft.getInstance().screen.width - backgroundWidth) / 2); // X-coordinate of the top-left corner
                    guiY = (int) (vector4f.y() - (Minecraft.getInstance().screen.height - backgroundHeight) / 2);
                    Slot slot = new MutableSlot(editContext.getLinkedInventory(), start + counter.getAndAdd(1), guiX, guiY);
                    currentSlots.add(slot);
                    editContext.addSlot(slot);
                });
            }
        });
        addChild(widget);

    }

    @Override
    public void renderWidget(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        currentMatrix = drawContext.pose().last().pose();
        if (firstRender) {
            update();
            firstRender = false;
        }
        //only previewStack on inventory change?
        // Add the initial GUI to the screen
        if (!craftingGuis.isEmpty()) {
            addGui(craftingGuis.get(currentGuiIndex));
        } else {
            addChild(fallbackCraftingWidget);
        }
        super.renderWidget(drawContext, mouseX, mouseY, delta);
    }

    public void setBuffers() {
        Map<String, String> data = new HashMap<>(defaultMap);
        action.forEachCraftingProperty(editContext.getItemstack(), ((craftingProperty, moduleInstance, itemStacks, invStart, invEnd, buf) -> {
            int index = craftingProperties.indexOf(craftingProperty);
            if (index >= 0) {
                craftingProperty.writeData(data, craftingGuis.get(craftingProperties.indexOf(craftingProperty)), editContext);
            } else {
                craftingProperty.writeData(data, null, editContext);
            }
        }));
        action.setData(data);
    }

    public static class PageButton<T> extends SimpleButton<T> {
        private final boolean isLeft;
        private final ResourceLocation texture;
        private boolean isClicked;
        private final ResourceLocation right = ResourceLocation.fromNamespaceAndPath(Miapi.MOD_ID, "textures/button_right.png");
        private final ResourceLocation left = ResourceLocation.fromNamespaceAndPath(Miapi.MOD_ID, "textures/button_left.png");

        public PageButton(int x, int y, int width, int height, boolean isLeft, T toCallBack, Consumer<T> callback) {
            super(x, y, width, height, Component.empty(), toCallBack, callback);
            this.isLeft = isLeft;
            if (isLeft) {
                texture = left;
            } else {
                texture = right;
            }
        }

        @Override
        public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();

            int textureOffset = 0;

            if (isClicked) {
                textureOffset = 20;
                if (!isMouseOver(mouseX, mouseY)) {
                    isClicked = false;
                }
            } else if (this.isMouseOver(mouseX, mouseY)) {
                textureOffset = 10;
            }

            if (!this.isEnabled) {
                textureOffset = 30;
            }

            context.blit(texture, getX(), getY(), 0, textureOffset, 0, this.width, this.height, 40, this.height);
        }
    }

    public class CraftButton<T> extends SimpleButton<T> {
        public HoverDescription hover;

        public CraftButton(int x, int y, int width, int height, Component title, T toCallback, Consumer<T> callback) {
            super(x, y, width, height, title, toCallback, callback);
            hover = new HoverDescription(x, y + height, List.of());
        }

        @Override
        public void renderWidget(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
            super.renderWidget(drawContext, mouseX, mouseY, delta);
        }

        @Override
        public void renderHover(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
            if (!this.isEnabled && isMouseOver(mouseX, mouseY)) {
                MutableComponent text = Component.translatable(Miapi.MOD_ID + ".ui.craft.warning").withStyle(ChatFormatting.RED);
                hover.addText(text);
                hover.setX(this.getX());
                hover.setY(this.getY() + this.getHeight());

                for (Component warning : warnings) {
                    hover.addText(Component.literal(" - ").append(warning).withStyle(ChatFormatting.RED));
                }
                hover.render(drawContext, mouseX, mouseY, delta);
                hover.reset();
            }
        }
    }
}
