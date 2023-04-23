package smartin.miapi.client.gui.crafting.crafter;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.architectury.event.events.client.ClientTooltipEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.MutableSlot;
import smartin.miapi.client.gui.SimpleButton;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.properties.SlotProperty;
import smartin.miapi.item.modular.properties.CraftingProperty;
import smartin.miapi.network.Networking;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class CraftView extends InteractAbleWidget {
    ItemStack compareStack;
    ItemStack originalStack;
    SlotProperty.ModuleSlot slotToChange;
    ItemModule replacingModule;
    String packetId;
    Inventory linkedInventory;
    CraftAction action;
    SimpleButton craftButton;
    SimpleButton previousButton;
    SimpleButton nextButton;
    List<CraftingProperty> craftingProperties = new ArrayList<>();
    List<InteractAbleWidget> craftingGuis = new ArrayList<>();
    int inventoryOffset;
    public static List<Slot> currentSlots = new ArrayList<>();
    private int currentGuiIndex = 0;
    Consumer<Slot> removeSlot;
    Consumer<Slot> addSlot;
    Consumer<ItemStack> preview;
    int backgroundWidth = 278;
    int backgroundHeight = 221;

    /**
     * This is a Widget build to support Children and parse the events down to them.
     * Best use in conjunction with the ParentHandledScreen as it also handles Children correct,
     * unlike the base vanilla classes.
     * If you choose to handle some Events yourself and want to support Children yourself, you need to call the correct
     * super method or handle the children yourself
     *
     * @param x      the X Position
     * @param y      the y Position
     * @param width  the width
     * @param height the height
     *               These for Params above are used to create feedback on isMouseOver() by default
     */
    public CraftView(int x, int y, int width, int height, String packetId, ItemModule module, ItemStack stack, Inventory inventory, int offset, SlotProperty.ModuleSlot slot, Consumer<SlotProperty.ModuleSlot> back, Consumer<ItemStack> newStack, Consumer<Slot> addSlot, Consumer<Slot> removeSlot) {
        super(x, y, width, height, Text.empty());
        compareStack = stack;
        this.preview = newStack;
        linkedInventory = inventory;
        inventoryOffset = offset;
        originalStack = stack;
        slotToChange = slot;
        this.packetId = packetId;
        replacingModule = module;
        this.addSlot = addSlot;
        this.removeSlot = removeSlot;
        action = new CraftAction(originalStack, slotToChange, module, MinecraftClient.getInstance().player, null);
        action.linkInventory(inventory, offset);
        compareStack = action.getPreview();
        action.forEachCraftingProperty(compareStack, ((craftingProperty, moduleInstance, itemStacks, invStart, invEnd, buf) -> {
            InteractAbleWidget guiScreen = craftingProperty.createGui(this.x, this.y, this.width, this.height - 30);
            if (guiScreen != null) {
                craftingGuis.add(guiScreen);
                craftingProperties.add(craftingProperty);
            }
        }));
        addChild(new SimpleButton<>(this.x + 10, this.y + this.height - 10, 40, 10, Text.translatable(Miapi.MOD_ID + ".ui.craft.back"), slot, back::accept));

        if (craftingGuis.size() > 1) {
            previousButton = new PageButton<>(this.x + this.width - 10, this.y, 10, 10, true, null, (callback) -> {
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
            nextButton = new PageButton<>(this.x + 10, this.y, 10, 10, false, null, (callback) -> {
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

        // Add the initial GUI to the screen
        if (craftingGuis.size() > 0) {
            addGui(craftingGuis.get(currentGuiIndex));
        }

        craftButton = new SimpleButton<>(this.x + this.width - 50, this.y + this.height - 10, 40, 10, Text.translatable(Miapi.MOD_ID + ".ui.craft"), null, (callback) -> {
            setBuffers();
            if (action.canPerform()) {
                Networking.sendC2S(packetId, action.toPacket(Networking.createBuffer()));
                newStack.accept(action.getPreview());
            }
        });
        addChild(craftButton);
    }

    public void closeSlot() {
        currentSlots.forEach(slot -> {
            removeSlot.accept(slot);
        });
    }

    public void addGui(InteractAbleWidget widget) {
        widget.x = this.x;
        widget.y = this.y + 15;
        widget.setWidth(this.width);
        widget.setHeight(this.height - 30);
        //closeSlot();
        currentSlots.forEach(slot -> {
            if (slot instanceof MutableSlot mutableSlot) {
                mutableSlot.setEnabled(false);
            }
        });
        CraftingProperty property = craftingProperties.get(craftingGuis.indexOf(widget));
        action.forEachCraftingProperty(compareStack, (craftingProperty, module, inventory, start, end, buffer) -> {
            if (craftingProperty.equals(property)) {
                AtomicInteger counter = new AtomicInteger(0);
                property.getSlotPositions().forEach(vec2f -> {
                    int guiX = (MinecraftClient.getInstance().currentScreen.width - backgroundWidth) / 2; // X-coordinate of the top-left corner
                    int guiY = (MinecraftClient.getInstance().currentScreen.height - backgroundHeight) / 2;
                    Slot slot = new MutableSlot(linkedInventory, start + counter.getAndAdd(1), (widget.x + (int) vec2f.x) - guiX, widget.y + (int) vec2f.y - guiY);
                    currentSlots.add(slot);
                    addSlot.accept(slot);
                });
            }
        });
        addChild(widget);

    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        //only preview on inventory change?
        preview.accept(action.getPreview());
        setBuffers();
        craftButton.isEnabled = action.canPerform();
        super.render(matrices, mouseX, mouseY, delta);
    }

    public void linkInventory(Inventory inventory, int offset) {
        this.linkedInventory = inventory;
        this.inventoryOffset = offset;
    }

    public void setBuffers() {
        List<PacketByteBuf> buffers = new ArrayList<>();
        action.forEachCraftingProperty(compareStack, ((craftingProperty, moduleInstance, itemStacks, invStart, invEnd, buf) -> {
            PacketByteBuf buf1 = Networking.createBuffer();
            int index = craftingProperties.indexOf(craftingProperty);
            if (index >= 0) {
                craftingProperty.writeCraftingBuffer(buf1, craftingGuis.get(craftingProperties.indexOf(craftingProperty)));
            }
            buffers.add(buf1);
        }));
        action.setBuffer(buffers.toArray(new PacketByteBuf[0]));
    }

    public ItemStack compareStack() {
        return compareStack;
    }

    public class PageButton<T> extends SimpleButton<T> {
        private boolean isLeft;
        private Identifier texture;
        private boolean isClicked;
        private Identifier right = new Identifier(Miapi.MOD_ID, "textures/button_right.png");
        private Identifier left = new Identifier(Miapi.MOD_ID, "textures/button_left.png");

        public PageButton(int x, int y, int width, int height, boolean isLeft, T toCallBack, Consumer<T> callback) {
            super(x, y, width, height, Text.empty(), toCallBack, callback);
            this.isLeft = isLeft;
            if (isLeft) {
                texture = left;
            } else {
                texture = right;
            }
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            super.render(matrices, mouseX, mouseY, delta);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
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

            drawTexture(matrices, x, y, 0, textureOffset, 0, this.width, this.height, 40, this.height);
        }
    }
}
