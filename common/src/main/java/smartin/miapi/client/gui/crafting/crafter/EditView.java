package smartin.miapi.client.gui.crafting.crafter;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.client.gui.*;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.modules.properties.slot.SlotProperty;
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
    ModuleInstance instance;
    Consumer<ItemStack> previewConsumer;
    Consumer<Object> back;
    List<GuiEventListener> defaultChildren = new ArrayList<>();
    EditOption.EditContext editContext;
    SlotProperty.ModuleSlot slot;
    List<Slot> currentSlots = new ArrayList<>();

    public EditView(int x, int y, int width, int height, ItemStack stack, @Nullable SlotProperty.ModuleSlot slot, Consumer<ItemStack> preview, Consumer<Object> back) {
        super(x, y, width, height, Component.empty());
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


        ScrollingTextWidget header = new ScrollingTextWidget((int) ((this.getX() + 5) / headerScale), (int) (this.getY() / headerScale), (int) ((this.width - 10) / headerScale), Component.translatable(Miapi.MOD_ID + ".ui.edit.header"), FastColor.ARGB32.color(255, 255, 255, 255));
        headerHolder.addChild(header);
        ScrollList list = new ScrollList(x, y + 16, width, height - 14, new ArrayList<>());
        defaultChildren.add(list);
        list.children().clear();
        defaultChildren.add(new SimpleButton<>(this.getX() + 2, this.getY() + this.height - 10, 40, 12, Component.translatable(Miapi.MOD_ID + ".ui.back"), stack, back));
        ArrayList<InteractAbleWidget> toList = new ArrayList<>();
        editContext = new EditOption.EditContext() {
            @Override
            public void craft(FriendlyByteBuf packetByteBuf) {
            }

            @Override
            public void preview(FriendlyByteBuf preview) {

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
            public @Nullable ModuleInstance getInstance() {
                return instance;
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
        RegistryInventory.editOptions.getFlatMap().forEach((s, editOption) -> {
            if (editOption.isVisible(editContext)) {
                ModuleInstance moduleInstance = null;
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
            if (Minecraft.getInstance().player.containerMenu instanceof CraftingScreenHandler handler) {
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
            public void craft(FriendlyByteBuf packetByteBuf) {
                AbstractContainerMenu screenHandler = this.getScreenHandler();
                if (screenHandler instanceof CraftingScreenHandler screenHandler1) {
                    ModuleInstance toCrafter = instance;
                    FriendlyByteBuf buf = Networking.createBuffer();
                    buf.writeUtf(RegistryInventory.editOptions.findKey(option));
                    List<String> position = new ArrayList<>();
                    if (toCrafter != null) {
                        toCrafter.calculatePosition(position);
                    }
                    String sharedPos = "";
                    if(!position.isEmpty()){
                        sharedPos = position.removeFirst();
                    }
                    for (String entry : position) {
                        sharedPos = sharedPos + "\n" + entry;
                    }
                    buf.writeUtf(sharedPos);
                    buf.writeBytes(packetByteBuf.copy());
                    Networking.sendC2S(screenHandler1.editPacketID, buf);
                }
                preview(packetByteBuf);
            }

            @Override
            public void preview(FriendlyByteBuf preview) {
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
            public @Nullable ModuleInstance getInstance() {
                return instance;
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
            public CraftingScreenHandler getScreenHandler() {
                if (Minecraft.getInstance().player.containerMenu instanceof CraftingScreenHandler craftingScreenHandler) {
                    return craftingScreenHandler;
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
            public void addSlot(Slot slot) {
                if (Minecraft.getInstance().player.containerMenu instanceof CraftingScreenHandler craftingScreenHandler) {
                    craftingScreenHandler.addSlotByClient(slot);
                    currentSlots.add(slot);
                }
            }

            @Override
            public void removeSlot(Slot slot) {
                if (Minecraft.getInstance().player.containerMenu instanceof CraftingScreenHandler craftingScreenHandler) {
                    craftingScreenHandler.removeSlotByClient(slot);
                    currentSlots.remove(slot);
                }
            }
        };
        this.children().clear();
        this.addChild(option.getGui(getX(), getY(), width, height, editContext));
    }

    class SlotButton extends InteractAbleWidget {
        private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Miapi.MOD_ID, "textures/gui/crafter/module_button_select.png");
        private final ScrollingTextWidget textWidget;
        private final EditOption option;


        public SlotButton(int x, int y, int width, int height, String editName, EditOption option, ModuleInstance instance) {
            super(x, y, width, height, Component.empty());
            Component translated = StatResolver.translateAndResolve(Miapi.MOD_ID + ".edit.option." + editName, instance);
            textWidget = new ScrollingTextWidget(0, 0, this.width, translated, FastColor.ARGB32.color(255, 255, 255, 255));
            this.option = option;
        }

        public void renderWidget(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
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
