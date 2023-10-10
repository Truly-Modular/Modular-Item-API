package smartin.miapi.client.gui.crafting.crafter.replace;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.client.gui.*;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.SlotProperty;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.network.Networking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * This Widget manages the actual craftView.
 * Its manages the replacing of a module with a new Module
 */
@Environment(EnvType.CLIENT)
public class CraftView extends InteractAbleWidget {
    ItemStack compareStack;
    ItemStack originalStack;
    SlotProperty.ModuleSlot slotToChange;
    ItemModule replacingModule;
    String packetId;
    Inventory linkedInventory;
    CraftAction action;
    CraftButton<Object> craftButton;
    SimpleButton<Object> previousButton;
    SimpleButton<Object> nextButton;
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
    boolean firstRender = true;
    boolean isClosed = false;

    public CraftView(int x, int y, int width, int height, String packetId,
                     ItemModule module,
                     ItemStack stack,
                     Inventory inventory,
                     int offset,
                     SlotProperty.ModuleSlot slot,
                     Consumer<SlotProperty.ModuleSlot> back,
                     Consumer<ItemStack> newStack,
                     Consumer<Slot> addSlot,
                     Consumer<Slot> removeSlot,
                     ScreenHandler handler) {
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

        ModularWorkBenchEntity bench = null;
        if (handler instanceof CraftingScreenHandler csh) bench = csh.blockEntity;

        if(replacingModule == null){
            replacingModule = ItemModule.empty;
        }
        action = new CraftAction(originalStack, slotToChange, module, MinecraftClient.getInstance().player, bench, null);
        action.linkInventory(inventory, offset);
        compareStack = action.getPreview();
        action.forEachCraftingProperty(compareStack, ((craftingProperty, moduleInstance, itemStacks, invStart, invEnd, buf) -> {
            InteractAbleWidget guiScreen = craftingProperty.createGui(this.getX(), this.getY(), this.width, this.height - 30, action);
            if (guiScreen != null) {
                craftingGuis.add(guiScreen);
                craftingProperties.add(craftingProperty);
            }
        }));
        addChild(new SimpleButton<>(this.getX() + 10, this.getY() + this.height - 10, 40, 12, Text.translatable(Miapi.MOD_ID + ".ui.back"), slot, (moduleSlot) -> {
            isClosed = true;
            back.accept(moduleSlot);
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

        craftButton = new CraftButton<>(this.getX() + this.width - 50, this.getY() + this.height - 10, 40, 12, Text.translatable(Miapi.MOD_ID + ".ui.craft"), null, (callback) -> {
            setBuffers();
            if (action.canPerform()) {
                isClosed = true;
                ItemStack craftedStack = action.getPreview();
                if (!ItemStack.areEqual(stack, craftedStack)) {
                    Networking.sendC2S(packetId, action.toPacket(Networking.createBuffer()));
                    newStack.accept(craftedStack);
                }
            }
        });
        addChild(craftButton);

        handler.addListener(new SimpleScreenHandlerListener((h, slotId, itemStack) -> {
            /*if (currentSlots.stream().anyMatch(slot1 -> slot1.id == slotId)) {
                    update();
                }*/
            if (slotId != 36) {
                update();
            }
        }));
    }

    private void update() {
        try {
            if (!isClosed) {
                ItemStack previewStack = action.getPreview();
                preview.accept(previewStack);
                setBuffers();
                Pair<Map<CraftingProperty, Boolean>, Boolean> canPerform = action.fullCanPerform();
                craftButton.isEnabled = canPerform.getSecond();

                warnings.clear();
                if (ItemStack.areEqual(previewStack, originalStack)) {
                    warnings.add(Text.translatable(Miapi.MOD_ID + ".ui.craft.result_equal_warning"));
                    craftButton.isEnabled = false;
                }
                if(previewStack.getDamage() > previewStack.getMaxDamage()){
                    warnings.add(Text.translatable(Miapi.MOD_ID + ".ui.craft.warning.durability_negative"));
                    craftButton.isEnabled = false;
                }
                canPerform.getFirst().forEach((property, result) -> {
                    if (!result) {
                        Text warning = property.getWarning();
                        if (warning != null && !warning.getString().isEmpty())
                            warnings.add(warning);
                    }
                });
            }
        } catch (Exception e) {

        }
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
        widget.setX(this.getX());
        widget.setY(this.getY());
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
                    int guiX = widget.getX() + (int) vec2f.x;
                    int guiY = widget.getY() + (int) vec2f.y;
                    Matrix4f inverse = new Matrix4f(currentMatrix);
                    Vector4f vector4f = TransformableWidget.transFormMousePos(guiX, guiY, inverse);
                    guiX = (int) (vector4f.x() - (MinecraftClient.getInstance().currentScreen.width - backgroundWidth) / 2); // X-coordinate of the top-left corner
                    guiY = (int) (vector4f.y() - (MinecraftClient.getInstance().currentScreen.height - backgroundHeight) / 2);
                    Slot slot = new MutableSlot(linkedInventory, start + counter.getAndAdd(1), guiX, guiY);
                    currentSlots.add(slot);
                    addSlot.accept(slot);
                });
            }
        });
        addChild(widget);

    }

    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        currentMatrix = drawContext.getMatrices().peek().getPositionMatrix();
        if (firstRender) {
            update();
            firstRender = false;
        }
        //only previewStack on inventory change?
        // Add the initial GUI to the screen
        if (!craftingGuis.isEmpty()) {
            addGui(craftingGuis.get(currentGuiIndex));
        }
        super.render(drawContext, mouseX, mouseY, delta);
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

    public static class PageButton<T> extends SimpleButton<T> {
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
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
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

            context.drawTexture(texture, getX(), getY(), 0, textureOffset, 0, this.width, this.height, 40, this.height);
        }
    }

    public class CraftButton<T> extends SimpleButton<T> {
        public HoverDescription hover;

        public CraftButton(int x, int y, int width, int height, Text title, T toCallback, Consumer<T> callback) {
            super(x, y, width, height, title, toCallback, callback);
            hover = new HoverDescription(x, y + height, List.of());
        }

        @Override
        public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
            super.render(drawContext, mouseX, mouseY, delta);
        }

        @Override
        public void renderHover(DrawContext drawContext, int mouseX, int mouseY, float delta) {
            if (!this.isEnabled && isMouseOver(mouseX, mouseY)) {
                MutableText text = Text.translatable(Miapi.MOD_ID + ".ui.craft.warning").formatted(Formatting.RED);
                hover.addText(text);
                hover.setX(this.getX());
                hover.setY(this.getY() + this.getHeight());

                for (Text warning : warnings) {
                    hover.addText(Text.literal(" - ").append(warning).formatted(Formatting.RED));
                }
                hover.render(drawContext, mouseX, mouseY, delta);
                hover.reset();
            }
        }
    }
}
