package smartin.miapi.client.gui.crafting.crafter;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.properties.SlotProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ModuleCrafter extends InteractAbleWidget {
    private ItemStack stack;
    private ItemModule module;
    private SlotProperty.ModuleSlot slot;
    private final Consumer<ItemStack> preview;
    private final Consumer<SlotProperty.ModuleSlot> selectedSlot;
    private SlotProperty.ModuleSlot baseSlot = new SlotProperty.ModuleSlot(new ArrayList<>());
    private static final Identifier BACKGROUND_TEXTURE = new Identifier(Miapi.MOD_ID, "textures/crafting_gui_background_black.png");
    private String paketIdentifier;
    private Inventory linkedInventory;
    CraftView craftView;
    Consumer<Slot> removeSlot;
    Consumer<Slot> addSlot;

    public ModuleCrafter(int x, int y, int width, int height, Consumer<SlotProperty.ModuleSlot> selected, Consumer<ItemStack> craftedItem, Inventory linkedInventory, Consumer<Slot> addSlot, Consumer<Slot> removeSlot) {
        super(x, y, width, height, Text.empty());
        this.linkedInventory = linkedInventory;
        //set Header, current Module Selected
        List<ClickableWidget> widgets = new ArrayList<>();
        this.preview = craftedItem;
        this.selectedSlot = selected;
        this.removeSlot = removeSlot;
        this.addSlot = addSlot;
        CraftView.currentSlots.forEach(slot1 -> {
            removeSlot.accept(slot1);
        });
    }

    public void setItem(ItemStack stack) {
        this.stack = stack;
    }

    public void setSelectedSlot(SlotProperty.ModuleSlot instance) {
        slot = instance;
        setMode(Mode.DETAIL);
    }

    public void setBaseSlot(SlotProperty.ModuleSlot instance) {
        baseSlot = instance;
    }

    public void setPacketIdentifier(String identifier) {
        paketIdentifier = identifier;
    }

    private void selectSlot(SlotProperty.ModuleSlot slot) {
        selectedSlot.accept(slot);
        setSelectedSlot(slot);
    }

    public void setMode(Mode mode) {
        if (!mode.equals(Mode.CRAFT)) {
            preview.accept(stack);
        }
        if (craftView != null) {
            craftView.closeSlot();
            craftView = null;
        }
        switch (mode) {
            case DETAIL -> {
                this.children().clear();
                DetailView detailView = new DetailView(this.x, this.y, this.width, this.height - 38, this.baseSlot, this.slot,
                        toEdit -> {
                            setMode(Mode.EDIT);
                        },
                        toReplace -> {
                            if (toReplace == null) {
                                List<String> allowed = new ArrayList<>();
                                allowed.add("");
                                allowed.add("melee");
                                toReplace = new SlotProperty.ModuleSlot(allowed);
                            }
                            slot = toReplace;
                            setMode(Mode.REPLACE);
                        });
                this.children.add(detailView);
            }
            case CRAFT -> {
                craftView = new CraftView(this.x, this.y, this.width, this.height - 38, paketIdentifier, module, stack, linkedInventory, 1, slot, (backSlot) -> {
                    setSelectedSlot(backSlot);
                }, (replaceItem) -> {
                    preview.accept(replaceItem);
                }, addSlot, removeSlot);
                this.children().clear();
                this.addChild(craftView);
            }
            case EDIT -> {
                Miapi.LOGGER.error("edit");
            }
            case REPLACE -> {
                this.children.clear();
                ReplaceView view = new ReplaceView(this.x, this.y, this.width, this.height - 38, slot, (instance) -> {
                    setSelectedSlot(instance);
                }, (module -> {
                    this.module = module;
                    setMode(Mode.CRAFT);
                }), (module -> {
                    CraftAction action = new CraftAction(stack, slot, module, null, new PacketByteBuf[0]);
                    action.linkInventory(linkedInventory, 1);
                    preview.accept(action.getPreview());
                }));
                addChild(view);
            }
        }
    }

    private void renderBackground(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        //RENDER Selected Module Top
        Text moduleName = Text.translatable(Miapi.MOD_ID + ".module.empty");
        try {
            moduleName = Text.translatable(Miapi.MOD_ID + ".module." + slot.inSlot.module.getName());
        } catch (Exception e) {

        }
        //drawSquareBorder(matrices,this.x,this.y,this.width,16,2,9145227);
        drawSquareBorder(matrices, this.x, this.y, this.width, 15, 2, ColorHelper.Argb.getArgb(255, 139, 139, 139));
        MinecraftClient.getInstance().textRenderer.draw(matrices, moduleName, this.x + 4, this.y + 4, ColorHelper.Argb.getArgb(255, 59, 59, 59));
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        //this.drawTexture(matrices, this.x, this.y, 0, 0, this.width, this.height);
        //drawText
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        //renderBackground(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }

    public enum Mode {
        DETAIL,
        EDIT,
        REPLACE,
        CRAFT
    }

    public class BottomButton extends InteractAbleWidget {
        private static final Identifier ButtonTexture = new Identifier(Miapi.MOD_ID, "textures/button.png");
        public ItemModule.ModuleInstance moduleInstance;

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
         * @param title
         */
        public BottomButton(int x, int y, int width, int height, Text title) {
            super(x, y, width, height, title);
        }

        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            this.renderButton(matrices, mouseX, mouseY, delta);
            super.render(matrices, mouseX, mouseY, delta);
        }

        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, ButtonTexture);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();

            int textureSize = 30;
            int textureOffset = 0;

            drawTexture(matrices, x, y, 0, textureOffset, 0, this.width, this.height, this.width, this.height);
        }
    }
}
