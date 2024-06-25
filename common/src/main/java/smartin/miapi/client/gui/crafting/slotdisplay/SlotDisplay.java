package smartin.miapi.client.gui.crafting.slotdisplay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.modules.properties.SlotProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Provides a 3d Interactive view of the Item, implements Button based on provided baseSlot
 * is this class a mess? yes. yes it is.
 * does it work? more or less
 */
@Environment(EnvType.CLIENT)
public class SlotDisplay extends InteractAbleWidget {
    private final Map<SlotProperty.ModuleSlot, ModuleButton> buttonMap = new HashMap<>();
    private ItemStack stack;
    private MatrixStack slotProjection = new MatrixStack();
    private double lastMouseX;
    private double lastMouseY;
    private boolean mouseDown0 = false;
    private boolean mouseDown1 = false;
    private SlotProperty.ModuleSlot selected = null;
    private final Consumer<SlotProperty.ModuleSlot> setSelected;
    private SlotProperty.ModuleSlot baseSlot;

    public SlotDisplay(ItemStack stack, int x, int y, int height, int width, Consumer<SlotProperty.ModuleSlot> selected) {
        super(x, y, width, height, Text.literal("Item Display"));
        this.stack = stack;
        this.height = height;
        this.width = width;
        slotProjection.scale(1.0F, -1.0F, 1.0F);
        this.setSelected = selected;
        this.setBaseSlot(new SlotProperty.ModuleSlot(new ArrayList<>()));
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (mouseDown0) {
            handleLeftClickDrag(mouseX, mouseY, lastMouseX - mouseX, lastMouseY - mouseY);
        } else if (mouseDown1) {
            handleRightClickDrag(mouseX, mouseY, lastMouseX - mouseX, lastMouseY - mouseY);
        }
        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    @Override
    public boolean isMouseOver(double x, double y) {
        boolean mouseOver = x >= this.getX() && y >= this.getY() && x < this.getX() + this.width && y < this.getY() + this.height;
        if (!mouseOver) {
            mouseDown0 = false;
            mouseDown1 = false;
        } else {
            return true;
        }
        return super.isMouseOver(x, y);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOver(mouseX, mouseY)) {
            mouseDown0 = true;
        } else if (button == 1 && isMouseOver(mouseX, mouseY)) {
            mouseDown1 = true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            mouseDown0 = false;
        } else if (button == 1) {
            mouseDown1 = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void handleLeftClickDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        MatrixStack newStack = new MatrixStack();
        newStack.translate((float) -deltaX / 100, -(float) deltaY / 100, 0);
        newStack.multiplyPositionMatrix(slotProjection.peek().getPositionMatrix());
        slotProjection = newStack;
    }

    private void handleRightClickDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        float angleX = (float) -(deltaY * 0.02f);
        float angleY = (float) -(deltaX * 0.02f);
        MatrixStack newStack = new MatrixStack();
        newStack.peek().getPositionMatrix().rotateAffineXYZ(-angleX, angleY, 0);
        newStack.multiplyPositionMatrix(slotProjection.peek().getPositionMatrix());
        slotProjection = newStack;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (isMouseOver(mouseX, mouseY)) {
            double scale = Math.pow(2, amount / 10);
            slotProjection.scale((float) scale, (float) scale, (float) scale);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    public void setBaseSlot(SlotProperty.ModuleSlot baseSlot1) {
        baseSlot = baseSlot1;
        buttonMap.forEach((slot1, moduleButton) -> {
            children().remove(moduleButton);
        });
        buttonMap.clear();
        if (baseSlot != null && baseSlot.inSlot != null) {
            baseSlot.inSlot.allSubModules().forEach(moduleInstances -> {
                SlotProperty.getSlots(moduleInstances).forEach((number, slot) -> {
                    buttonMap.computeIfAbsent(slot, newSlot -> {
                        ModuleButton newButton = new ModuleButton(0, 0, 10, 10, newSlot);
                        addChild(newButton);
                        return newButton;
                    });
                });
            });
            buttonMap.computeIfAbsent(baseSlot, newSlot -> {
                ModuleButton newButton = new ModuleButton(0, 0, 10, 10, newSlot);
                addChild(newButton);
                return newButton;
            });
        }
    }

    public void setItem(ItemStack itemStack) {
        stack = itemStack;
        slotProjection = new MatrixStack();
        slotProjection.scale(1.0F, -1.0F, 1.0F);
        selected = new SlotProperty.ModuleSlot(new ArrayList<>());
    }

    public int getSize() {
        int size = Math.min(width, height);
        size = Math.max(5, size - 10);
        return size;
    }

    @Override
    public void renderWidget(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        drawContext.enableScissor(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height);
        renderSlot(stack, drawContext, mouseX, mouseY, delta);
        super.render(drawContext, mouseX, mouseY, delta);
        drawContext.disableScissor();
    }

    public void select(SlotProperty.ModuleSlot selected) {
        this.selected = selected;
    }

    private Vector3f position() {
        return new Vector3f(getX() + (float) (width - 16) / 2, getY() + (float) (height - 16) / 2, (100.0F + 50));
    }

    public void renderSlot(ItemStack stack, DrawContext context, int mouseX, int mouseY, float delta) {
        ItemRenderer renderer = MinecraftClient.getInstance().getItemRenderer();
        MinecraftClient.getInstance().getTextureManager().getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).setFilter(false, false);
        RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        Matrix4fStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.pushMatrix();
        Vector3f pos = position();
        matrixStack.translate(pos.x(), pos.y(), pos.z());
        matrixStack.scale(getSize(), getSize(), 1.0F);
        RenderSystem.applyModelViewMatrix();
        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        boolean bl = true;
        if (bl) {
            DiffuseLighting.disableGuiDepthLighting();
        }
        RenderSystem.enableDepthTest();
        renderer.renderItem(stack, ModelTransformationMode.GUI, 15728880, OverlayTexture.DEFAULT_UV, slotProjection, immediate, MinecraftClient.getInstance().world, 0);
        immediate.draw();
        RenderSystem.enableDepthTest();
        if (bl) {
            DiffuseLighting.enableGuiDepthLighting();
        }
        matrixStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    public class ModuleButton extends InteractAbleWidget {
        private static final Identifier ButtonTexture = Identifier.of(Miapi.MOD_ID, "textures/button.png");
        public SlotProperty.ModuleSlot instance;

        public ModuleButton(int x, int y, int width, int height, SlotProperty.ModuleSlot instance) {
            super(x, y, width, height, Text.literal(" "));
            this.instance = instance;
        }

        private void setSelected(SlotProperty.ModuleSlot instance) {
            selected = instance;
            setSelected.accept(instance);
        }

        public void renderWidget(DrawContext drawContext, int mouseX, int mouseY, float delta) {
            RenderSystem.depthFunc(GL11.GL_ALWAYS);
            RenderSystem.disableDepthTest();
            this.renderButton(drawContext, mouseX, mouseY, delta);
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.isMouseOver(mouseX, mouseY)) {
                playClickedSound();
                setSelected(this.instance);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        public void renderButton(DrawContext drawContext, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();

            int textureSize = 30;
            int textureOffset = 0;

            if (this.instance.equals(selected)) {
                textureOffset = 20;
            } else if (this.isMouseOver(mouseX, mouseY)) {
                textureOffset = 10;
            }

            //drawTexture(drawContext, ButtonTexture, getX(), getY(), 0, textureOffset, 0, this.width, this.height, textureSize, 10);
        }
    }
}
