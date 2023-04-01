package smartin.miapi.client.gui.crafting.moduleCrafter;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.BoxList;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.properties.SlotProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ModuleCrafter extends InteractAbleWidget {
    public BoxList list;
    private ItemStack stack;
    private ItemModule module;
    private Mode mode = Mode.DETAIL;
    private ItemModule.ModuleInstance selected;
    private SlotProperty.ModuleSlot slot;
    private List<Consumer<ItemModule.ModuleInstance>> callbacks = new ArrayList<>();

    public ModuleCrafter(int x, int y, int width, int height) {

        super(x, y, width, height, Text.empty());
        //set Header, current Module Selected
        List<ClickableWidget> widgets = new ArrayList<>();

        list = new BoxList(this.x, this.y + 18, this.width, this.height - 38, Text.empty(), widgets);
        list.setSpace(1);
        this.addChild(list);
    }

    public void registerCallBack(Consumer<ItemModule.ModuleInstance> callback) {
        callbacks.add(callback);
    }

    public void setItem(ItemStack stack) {
        this.stack = stack;
    }

    public void select(ItemModule.ModuleInstance instance) {
        this.selected = instance;
        setMode(Mode.DETAIL);
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        switch (mode) {
            case DETAIL -> {
                this.children().clear();
                DetailView detailView = new DetailView(this.x, this.y + 18, this.width, this.height - 38, this.selected, (test) -> {
                    Miapi.LOGGER.error("toEdit");
                    setMode(Mode.EDIT);
                }, (test) -> {
                    if(test==null){
                        List<String> allowed = new ArrayList<>();
                        allowed.add("");
                        allowed.add("melee");
                        test = new SlotProperty.ModuleSlot(allowed);
                    }
                    slot = test;
                    Miapi.LOGGER.error(test.toString());
                    Miapi.LOGGER.error("toReplace");
                    setMode(Mode.REPLACE);
                });
                detailView.registerCallBack(this::setSelected);
                this.registerCallBack(detailView::select);
                this.children.add(detailView);
            }
            case CRAFT -> {
                Miapi.LOGGER.error("craft");
                CraftView craftView = new CraftView(this.x, this.y + 18, this.width, this.height - 38, module, stack, slot, (replaceItem) -> {
                    Miapi.server.getPlayerManager().getPlayerList().forEach(player -> {
                        player.getInventory().setStack(2, replaceItem);
                    });
                });
                this.children().clear();
                this.addChild(craftView);
                break;
            }
            case EDIT -> {
                Miapi.LOGGER.error("edit");
                break;
            }
            case REPLACE -> {
                Miapi.LOGGER.error("replace");
                this.children.clear();
                ReplaceView view = new ReplaceView(this.x, this.y + 18, this.width, this.height - 38, slot, (instance) -> {
                    Miapi.LOGGER.error("back");
                    setMode(Mode.DETAIL);
                }, (module -> {
                    Miapi.LOGGER.error("CraftModule");
                    this.module = module;
                    setMode(Mode.CRAFT);
                }));
                addChild(view);
                break;
            }
        }
    }

    private void setSelected(ItemModule.ModuleInstance instance) {
        this.selected = instance;
        callbacks.forEach(callback -> {
            callback.accept(instance);
        });
        setMode(Mode.DETAIL);
    }

    private void renderBackground(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        //RENDER Selected Module Top
        Text moduleName = Text.literal("No Module selected");
        if (selected != null) {
            moduleName = Text.translatable(Miapi.MOD_ID+".module."+selected.module.getName());
        }
        //drawSquareBorder(matrices,this.x,this.y,this.width,16,2,9145227);
        drawSquareBorder(matrices, this.x, this.y, this.width, 15, 2, ColorHelper.Argb.getArgb(255, 139, 139, 139));
        MinecraftClient.getInstance().textRenderer.draw(matrices, moduleName, this.x + 4, this.y + 4, ColorHelper.Argb.getArgb(255, 59, 59, 59));
        //drawText
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices, mouseX, mouseY, delta);
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

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            Miapi.LOGGER.warn("Clicked BoxButton " + this.getMessage());
            return true;
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
