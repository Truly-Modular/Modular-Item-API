package smartin.miapi.client.gui.crafting.moduleCrafter;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import org.lwjgl.opengl.GL11;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.BoxList;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.SimpleButton;
import smartin.miapi.item.modular.ItemModule;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DetailView extends InteractAbleWidget {
    BoxList list;
    private ItemModule.ModuleInstance selected;
    private List<Consumer<ItemModule.ModuleInstance>> callbacks = new ArrayList<>();
    private Consumer<ItemModule.ModuleInstance> edit;
    private Consumer<ItemModule.ModuleInstance> replace;

    public DetailView(int x, int y, int width, int height, ItemModule.ModuleInstance instance, Consumer<ItemModule.ModuleInstance> edit, Consumer<ItemModule.ModuleInstance> replace) {
        super(x, y, width, height, Text.empty());
        list = new BoxList(x, y, width, height - 11, Text.empty(), new ArrayList<>());
        addChild(list);
        this.edit = edit;
        this.replace = replace;
        this.setSelected(instance);
    }

    public void registerCallBack(Consumer<ItemModule.ModuleInstance> callback) {
        callbacks.add(callback);
    }

    public void select(ItemModule.ModuleInstance instance) {
        this.selected = instance;
    }

    private void setSelected(ItemModule.ModuleInstance instance) {
        this.selected = instance;
        callbacks.forEach(callback -> {
            callback.accept(instance);
        });
        update();
    }

    private void update() {
        list.children().clear();
        List<ClickableWidget> boxList = new ArrayList<>();
        SimpleButton editButton = new SimpleButton(this.x + this.width - 80, this.y + this.height - 10, 20, 10, Text.literal("Edit"), selected, (instance) -> {
            Miapi.LOGGER.error("clickedEdit2");
            edit.accept((ItemModule.ModuleInstance) instance);
        });
        SimpleButton replaceButton = new SimpleButton(this.x + this.width - 55, this.y + this.height - 10, 50, 10, Text.literal("Replace"), selected, (instance) -> {
            replace.accept((ItemModule.ModuleInstance) instance);
        });
        this.addChild(editButton);
        this.addChild(replaceButton);
        if (selected.parent != null) {
            boxList.add(new ModuleInstanceButton(0, 0, this.width - 10, 10, selected.parent, true));
        }
        if (selected.subModules != null) {
            selected.subModules.forEach((number, child) -> {
                boxList.add(new ModuleInstanceButton(0, 0, this.width - 10, 10, child));
            });
        }
        list.setWidgets(boxList, 1);
    }

    public class ModuleInstanceButton extends InteractAbleWidget {
        ItemModule.ModuleInstance instance;
        boolean indented;

        /**
         * This is a Widget build to support Children and parse the events down to them.
         * Best use in conjunction with the ParentHandledScreen as it also handles Children correct,
         * unlike the base vanilla classes.
         * If you choose to handle some Events yourself and want to support Children yourself, you need to call the correct
         * super method or handle the children yourself
         *
         * @param x        the X Position
         * @param y        the y Position
         * @param width    the width
         * @param height   the height
         *                 These for Params above are used to create feedback on isMouseOver() by default
         * @param instance
         */
        public ModuleInstanceButton(int x, int y, int width, int height, @Nonnull ItemModule.ModuleInstance instance) {
            this(x, y, width, height, instance, false);
        }

        public ModuleInstanceButton(int x, int y, int width, int height, @Nonnull ItemModule.ModuleInstance instance, boolean indented) {
            super(x, y, width, height, Text.literal(instance.module.getName()));
            this.instance = instance;
            this.indented = indented;
            if (this.indented) {
                this.setMessage(Text.literal("   " + instance.module.getName()));
            }
        }

        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.depthFunc(GL11.GL_ALWAYS);
            RenderSystem.depthMask(false);
            this.renderButton(matrices, mouseX, mouseY, delta);
            RenderSystem.depthFunc(GL11.GL_ALWAYS);
            RenderSystem.depthMask(true);
            super.render(matrices, mouseX, mouseY, delta);
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            Miapi.LOGGER.warn("Clicked BoxButton " + this.getMessage());
            setSelected(this.instance);
            return true;
        }

        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();

            int color = ColorHelper.Argb.getArgb(255, 50, 50, 50);

            if (selected == this.instance) {
                //selected
            } else if (this.isMouseOver(mouseX, mouseY)) {
                color = ColorHelper.Argb.getArgb(255, 100, 100, 100);
            }
            //drawSquareBorder(MatrixStack matrices, int x, int y, int width, int height, int borderWidth, int color)
            drawSquareBorder(matrices, this.x, this.y, this.width, this.height, 1, color);
            MinecraftClient.getInstance().textRenderer.draw(matrices, this.getMessage(), this.x + 2, this.y + 2, ColorHelper.Argb.getArgb(255, 59, 59, 59));
        }
    }
}
