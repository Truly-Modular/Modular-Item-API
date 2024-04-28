package smartin.miapi.client.gui.crafting.crafter;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.client.gui.*;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.modules.properties.SlotProperty;
import smartin.miapi.network.Networking;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * This is the selector screen to select witch EditProperty you want to interact with
 */
@Environment(EnvType.CLIENT)
public class EditView extends InteractAbleWidget {
    ItemStack stack;
    ItemModule.ModuleInstance instance;
    Consumer<ItemStack> previewConsumer;
    Consumer<Object> back;
    List<Element> defaultChildren = new ArrayList<>();
    EditOption.EditContext editContext;
    SlotProperty.ModuleSlot slot;
    List<Slot> currentSlots = new ArrayList<>();

    public EditView(int x, int y, int width, int height, ItemStack stack, @Nullable SlotProperty.ModuleSlot slot, Consumer<ItemStack> preview, Consumer<Object> back) {
        super(x, y, width, height, Text.empty());
        this.previewConsumer = preview;
        this.stack = stack;
        this.slot = slot;
        if (slot != null) {
            this.instance = slot.inSlot;
        }
        clearSlots();
        this.back = back;
        float headerScale = 1.5f;
        TransformableWidget headerHolder = new TransformableWidget(x, y, width, height, headerScale);
        defaultChildren.add(headerHolder);


        ScrollingTextWidget header = new ScrollingTextWidget((int) ((this.getX() + 5) / headerScale), (int) (this.getY() / headerScale), (int) ((this.width - 10) / headerScale), Text.translatable(Miapi.MOD_ID + ".ui.edit.header"), ColorHelper.Argb.getArgb(255, 255, 255, 255));
        headerHolder.addChild(header);
        ScrollList list = new ScrollList(x, y + 16, width, height - 14, new ArrayList<>());
        defaultChildren.add(list);
        list.children().clear();
        defaultChildren.add(new SimpleButton<>(this.getX() + 2, this.getY() + this.height - 10, 40, 12, Text.translatable(Miapi.MOD_ID + ".ui.back"), stack, back));
        ArrayList<InteractAbleWidget> toList = new ArrayList<>();
        editContext = new EditOption.EditContext() {
            @Override
            public void craft(PacketByteBuf packetByteBuf) {
            }

            @Override
            public void preview(PacketByteBuf preview) {

            }

            @Override
            public SlotProperty.ModuleSlot getSlot() {
                return slot;
            }

            @Override
            public ItemStack getItemstack() {
                return stack;
            }

            @Override
            public @Nullable ItemModule.ModuleInstance getInstance() {
                return instance;
            }

            @Override
            public @Nullable PlayerEntity getPlayer() {
                return MinecraftClient.getInstance().player;
            }

            @Override
            public @Nullable ModularWorkBenchEntity getWorkbench() {
                if (MinecraftClient.getInstance().player.currentScreenHandler instanceof CraftingScreenHandler craftingScreenHandler) {
                    return craftingScreenHandler.blockEntity;
                }
                return null;
            }

            @Override
            public Inventory getLinkedInventory() {
                if (MinecraftClient.getInstance().player.currentScreenHandler instanceof CraftingScreenHandler craftingScreenHandler) {
                    return craftingScreenHandler.inventory;
                }
                return null;
            }

            @Override
            public CraftingScreenHandler getScreenHandler() {
                if (MinecraftClient.getInstance().player.currentScreenHandler instanceof CraftingScreenHandler craftingScreenHandler) {
                    return craftingScreenHandler;
                }
                return null;
            }
        };
        RegistryInventory.editOptions.getFlatMap().forEach((s, editOption) -> {
            if (editOption.isVisible(editContext)) {
                ItemModule.ModuleInstance moduleInstance = null;
                if (instance != null) {
                    moduleInstance = instance.copy();
                }
                toList.add(new SlotButton(0, 0, this.width, 15, s, editOption, moduleInstance));
            }
        });

        list.setList(toList);
        setDefaultChildren();
    }

    public void clearSlots() {
        currentSlots.forEach(slot1 -> {
            if (MinecraftClient.getInstance().player.currentScreenHandler instanceof CraftingScreenHandler handler) {
                handler.removeSlotByClient(slot1);
            }
        });
    }

    private void setDefaultChildren() {
        this.children().clear();
        defaultChildren.forEach(this::addChild);
    }

    public void setEditOption(EditOption option) {
        editContext = new EditOption.EditContext() {
            @Override
            public void craft(PacketByteBuf packetByteBuf) {
                ScreenHandler screenHandler = this.getScreenHandler();
                if (screenHandler instanceof CraftingScreenHandler screenHandler1) {
                    ItemModule.ModuleInstance toCrafter = instance;
                    PacketByteBuf buf = Networking.createBuffer();
                    buf.writeString(RegistryInventory.editOptions.findKey(option));
                    List<Integer> position = new ArrayList<>();
                    if (toCrafter != null) {
                        toCrafter.calculatePosition(position);
                    } else {
                        if (slot != null && slot.parent != null) {
                            slot.parent.calculatePosition(position);
                            position.add(slot.id);
                        }
                    }
                    int[] positionArray = position.stream()
                            .mapToInt(Integer::intValue)
                            .toArray();
                    buf.writeIntArray(positionArray);
                    buf.writeBytes(packetByteBuf.copy());
                    Networking.sendC2S(screenHandler1.editPacketID, buf);
                }
                preview(packetByteBuf);
            }

            @Override
            public void preview(PacketByteBuf preview) {
                previewConsumer.accept(option.preview(preview, this));
            }

            @Override
            public SlotProperty.ModuleSlot getSlot() {
                return slot;
            }

            @Override
            public ItemStack getItemstack() {
                return stack;
            }

            @Override
            public @Nullable ItemModule.ModuleInstance getInstance() {
                return instance;
            }

            @Override
            public @Nullable PlayerEntity getPlayer() {
                return MinecraftClient.getInstance().player;
            }

            @Override
            public @Nullable ModularWorkBenchEntity getWorkbench() {
                if (MinecraftClient.getInstance().player.currentScreenHandler instanceof CraftingScreenHandler craftingScreenHandler) {
                    return craftingScreenHandler.blockEntity;
                }
                return null;
            }

            @Override
            public CraftingScreenHandler getScreenHandler() {
                if (MinecraftClient.getInstance().player.currentScreenHandler instanceof CraftingScreenHandler craftingScreenHandler) {
                    return craftingScreenHandler;
                }
                return null;
            }

            @Override
            public Inventory getLinkedInventory() {
                if (MinecraftClient.getInstance().player.currentScreenHandler instanceof CraftingScreenHandler craftingScreenHandler) {
                    return craftingScreenHandler.inventory;
                }
                return null;
            }

            @Override
            public void addSlot(Slot slot) {
                if (MinecraftClient.getInstance().player.currentScreenHandler instanceof CraftingScreenHandler craftingScreenHandler) {
                    craftingScreenHandler.addSlotByClient(slot);
                    currentSlots.add(slot);
                }
            }

            @Override
            public void removeSlot(Slot slot) {
                if (MinecraftClient.getInstance().player.currentScreenHandler instanceof CraftingScreenHandler craftingScreenHandler) {
                    craftingScreenHandler.removeSlotByClient(slot);
                    currentSlots.remove(slot);
                }
            }
        };
        this.children().clear();
        this.addChild(option.getGui(getX(), getY(), width, height, editContext));
    }

    class SlotButton extends InteractAbleWidget {
        private final Identifier texture = new Identifier(Miapi.MOD_ID, "textures/gui/crafter/module_button_select.png");
        private final ScrollingTextWidget textWidget;
        private final EditOption option;


        public SlotButton(int x, int y, int width, int height, String editName, EditOption option, ItemModule.ModuleInstance instance) {
            super(x, y, width, height, Text.empty());
            Text translated = StatResolver.translateAndResolve(Miapi.MOD_ID + ".edit.option." + editName, instance);
            textWidget = new ScrollingTextWidget(0, 0, this.width, translated, ColorHelper.Argb.getArgb(255, 255, 255, 255));
            this.option = option;
        }

        public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            int hoverOffset = 0;
            if (isMouseOver(mouseX, mouseY)) {
                hoverOffset = 14;
            }
            drawTextureWithEdge(drawContext, texture, this.getX(), this.getY(), 0, hoverOffset, 140, 14, this.getWidth(), this.getHeight(), 140, 42, 2);
            textWidget.setX(this.getX() + 2);
            textWidget.setY(this.getY() + 3);

            textWidget.setWidth(this.width - 4);
            textWidget.render(drawContext, mouseX, mouseY, delta);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isMouseOver(mouseX, mouseY) && (button == 0)) {
                setEditOption(option);
                return true;

            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
