package smartin.miapi.client.gui.crafting.crafter;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vector4f;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.*;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.SlotProperty;
import smartin.miapi.network.Networking;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class CraftView extends InteractAbleWidget {
    ItemStack compareStack;
    ItemStack originalStack;
    SlotProperty.ModuleSlot slotToChange;
    ItemModule replacingModule;
    String packetId;
    Inventory linkedInventory;
    CraftAction action;
    CraftButton craftButton;
    SimpleButton previousButton;
    SimpleButton nextButton;
    List<CraftingProperty> craftingProperties = new ArrayList<>();
    List<InteractAbleWidget> craftingGuis = new ArrayList<>();
    int inventoryOffset;
    public static List<Slot> currentSlots = new ArrayList<>();
    private int currentGuiIndex = 0;
    Matrix4f currentMatrix = new Matrix4f();
    Consumer<Slot> removeSlot;
    Consumer<Slot> addSlot;
    Consumer<ItemStack> preview;
    int backgroundWidth = 278;
    int backgroundHeight = 221;
    List<Text> warnings = new ArrayList<>();

    public CraftView(int x, int y, int width, int height, String packetId, ItemModule module, ItemStack stack, Inventory inventory, int offset, SlotProperty.ModuleSlot slot, Consumer<SlotProperty.ModuleSlot> back, Consumer<ItemStack> newStack, Consumer<Slot> addSlot, Consumer<Slot> removeSlot, ScreenHandler handler) {
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
            InteractAbleWidget guiScreen = craftingProperty.createGui(this.x, this.y, this.width, this.height - 30, action);
            if (guiScreen != null) {
                craftingGuis.add(guiScreen);
                craftingProperties.add(craftingProperty);
            }
        }));
        addChild(new SimpleButton<>(this.x + 10, this.y + this.height - 10, 40, 12, Text.translatable(Miapi.MOD_ID + ".ui.back"), slot, back::accept));

        if (craftingGuis.size() > 1) {
            previousButton = new PageButton<>(this.x + this.width - 10, this.y, 10, 12, true, null, (callback) -> {
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
            nextButton = new PageButton<>(this.x + 10, this.y, 10, 12, false, null, (callback) -> {
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

        craftButton = new CraftButton<>(this.x + this.width - 50, this.y + this.height - 10, 40, 12, Text.translatable(Miapi.MOD_ID + ".ui.craft"), null, (callback) -> {
            setBuffers();
            if (action.canPerform()) {
                Networking.sendC2S(packetId, action.toPacket(Networking.createBuffer()));
                newStack.accept(action.getPreview());
            }
        });
        addChild(craftButton);

        handler.addListener(new ScreenHandlerListener() {
            @Override
            public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
                try {
                    preview.accept(action.getPreview());
                    setBuffers();
                    craftButton.isEnabled = action.canPerform();

                    craftingProperties.forEach(craftingProperty -> {
                        warnings.clear();
                        Text warning = craftingProperty.getWarning();
                        if (warning != null && !warning.getString().isEmpty()) {
                            warnings.add(warning);
                        }
                    });

                } catch (Exception e) {

                }
            }

            @Override
            public void onPropertyUpdate(ScreenHandler handler, int property, int value) {

            }
        });
    }

    public void closeSlot() {
        currentSlots.forEach(slot -> {
            if (slot instanceof MutableSlot mutableSlot) {
                mutableSlot.setEnabled(true);
            }
            removeSlot.accept(slot);
        });
    }

    public void addGui(InteractAbleWidget widget) {
        if (children.contains(widget)) {
            return;
        }
        widget.x = this.x;
        widget.y = this.y;
        widget.setWidth(this.width);
        widget.setHeight(this.height - 30);
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
                    //int guiX = (MinecraftClient.getInstance().currentScreen.width - backgroundWidth) / 2; // X-coordinate of the top-left corner
                    //int guiY = (MinecraftClient.getInstance().currentScreen.height - backgroundHeight) / 2;
                    int guiX = widget.x + (int) vec2f.x;
                    int guiY = widget.y + (int) vec2f.y;
                    Matrix4f inverse = new Matrix4f(currentMatrix);
                    Vector4f vector4f = TransformableWidget.transFormMousePos(guiX, guiY, inverse);
                    guiX = (int) (vector4f.getX() - (MinecraftClient.getInstance().currentScreen.width - backgroundWidth) / 2); // X-coordinate of the top-left corner
                    guiY = (int) (vector4f.getY() - (MinecraftClient.getInstance().currentScreen.height - backgroundHeight) / 2);
                    //TransformableWidget.inverse(inverse);
                    Miapi.LOGGER.warn("X " + guiX + " Y " + guiY);
                    Miapi.LOGGER.warn("X " + vector4f.getX() + " Y " + vector4f.getY());
                    Slot slot = new MutableSlot(linkedInventory, start + counter.getAndAdd(1), guiX, guiY);
                    currentSlots.add(slot);
                    addSlot.accept(slot);
                });
            }
        });
        addChild(widget);

    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        currentMatrix = matrices.peek().getPositionMatrix();
        //only preview on inventory change?
        // Add the initial GUI to the screen
        addGui(craftingGuis.get(currentGuiIndex));
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

    public class CraftButton<T> extends SimpleButton<T> {
        public MultiLineTextWidget hover;

        public CraftButton(int x, int y, int width, int height, Text title, T toCallback, Consumer<T> callback) {
            super(x, y, width, height, title, toCallback, callback);
            hover = new MultiLineTextWidget(x, y, width, height, Text.empty());
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            super.render(matrices, mouseX, mouseY, delta);
        }

        @Override
        public void renderHover(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            String message = Text.translatable(Miapi.MOD_ID + ".ui.craft.warning").getString();
            hover.x = x;
            hover.y = y + this.height;

            for (Text text : warnings) {
                message += "\n - " + text.getString();
            }

            hover.setText(Text.of(message));
            if (!this.isEnabled) {
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                RenderSystem.enableDepthTest();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                hover.render(matrices, mouseX, mouseY, delta);
                RenderSystem.disableBlend();
                RenderSystem.enableTexture();
            }
        }
    }
}
