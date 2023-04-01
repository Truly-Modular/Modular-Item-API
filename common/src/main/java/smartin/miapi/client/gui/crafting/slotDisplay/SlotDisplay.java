package smartin.miapi.client.gui.crafting.slotDisplay;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vector4f;
import org.lwjgl.opengl.GL11;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.properties.SlotProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SlotDisplay extends InteractAbleWidget {
    private static final Identifier BACKGROUND_TEXTURE = new Identifier(Miapi.MOD_ID, "textures/crafting_gui_background_black.png");
    private final Map<ItemModule.ModuleInstance, ModuleButton> buttonMap = new HashMap<>();
    private ItemStack stack;
    private MatrixStack slotProjection = new MatrixStack();
    private double lastMouseX, lastMouseY;
    private boolean mouseDown0 = false;
    private boolean mouseDown1 = false;
    private ItemModule.ModuleInstance selected = null;
    private List<Consumer<ItemModule.ModuleInstance>> callbacks = new ArrayList<>();

    public void registerCallBack(Consumer<ItemModule.ModuleInstance> callback){
        callbacks.add(callback);
    }

    public void select(ItemModule.ModuleInstance instance){
        this.selected = instance;
    }

    private void setSelected(ItemModule.ModuleInstance instance){
        this.selected = instance;
        callbacks.forEach(callback->{
            callback.accept(instance);
        });
    }

    public SlotDisplay(ItemStack stack, int x, int y, int height, int width) {
        super(x, y, width, height, Text.literal("Item Display"));
        this.stack = stack;
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        //slotProjection.translate(x + (width - 16) / 2, y + (height - 16) / 2, (150.0F));
        //slotProjection.translate(8.0D, 8.0D, 0.0D);
        //slotProjection.translate(x + (width - 16) / 2, y + (height - 16) / 2, (150.0F));
        slotProjection.scale(1.0F, -1.0F, 1.0F);
        int size = Math.min(width, height);
        size = Math.max(5, size - 10);
        //slotProjection.scale(size, size, 16.0F);
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
        boolean mouseOver = x >= this.x && y >= this.y && x < this.x + this.width && y < this.y + this.height;
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
        if (button == 0) {
            mouseDown0 = true;
        } else if (button == 1) {
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
        slotProjection.translate((float) -deltaX / 100, (float) deltaY / 100, 0);
    }

    private void handleRightClickDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        float angleX = (float) -(deltaY * 0.02f);
        float angleY = (float) -(deltaX * 0.02f);
        slotProjection.multiply(Quaternion.fromEulerXyz(new Vec3f(angleX, angleY, 0)));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (isMouseOver(mouseX, mouseY)) {
            double scale = amount / 2;
            scale = Math.pow(2, amount / 10);
            slotProjection.scale((float) scale, (float) scale, 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }


    public void setItem(ItemStack itemStack) {
        stack = itemStack;
        buttonMap.clear();
    }

    public int getSize() {
        int size = Math.min(width, height);
        size = Math.max(5, size - 10);
        return size;
        //slotProjection.scale(size, size, 16.0F);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        renderSlot(stack, matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }

    private Vec3f position() {
        ItemRenderer renderer = MinecraftClient.getInstance().getItemRenderer();
        return new Vec3f(x + (width - 16) / 2, y + (height - 16) / 2, (100.0F + renderer.zOffset + 50));
    }

    public void renderBackground(MatrixStack matrices) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        this.drawTexture(matrices, this.x, this.y, 0, 46 + 20, this.width / 2, this.height);
        this.drawTexture(matrices, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + 20, this.width / 2, this.height);
    }

    public void renderSlot(ItemStack stack, MatrixStack matrices, int mouseX, int mouseY, float delta) {
        //GL11.GL_SCISSOR_TEST
        ItemRenderer renderer = MinecraftClient.getInstance().getItemRenderer();
        MinecraftClient.getInstance().getTextureManager().getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).setFilter(false, false);
        RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        Vec3f pos = position();
        matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
        //matrixStack.translate(8.0D, 8.0D, 0.0D);
        //matrixStack.scale(1.0F, -1.0F, 1.0F);
        matrixStack.scale(getSize(), getSize(), -16.0F);
        RenderSystem.applyModelViewMatrix();

        // Set the scissor region to limit the rendering area
        int scaleFactor = (int) MinecraftClient.getInstance().getWindow().getScaleFactor();
        //GL11.glEnable(GL11.GL_SCISSOR_TEST);
        //GL11.glScissor(x * scaleFactor, MinecraftClient.getInstance().getWindow().getFramebufferHeight() - (y + height) * scaleFactor, width * scaleFactor, height * scaleFactor);

        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        boolean bl = false;
        if (bl) {
            DiffuseLighting.disableGuiDepthLighting();
        }
        renderer.renderItem(stack, ModelTransformation.Mode.GUI, 15728880, OverlayTexture.DEFAULT_UV, slotProjection, immediate, 0);
        renderButtons(stack, slotProjection, matrixStack, this.height);
        immediate.draw();
        //GL11.glDisable(GL11.GL_SCISSOR_TEST);
        RenderSystem.enableDepthTest();
        if (bl) {
            DiffuseLighting.enableGuiDepthLighting();
        }
        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();
    }

    public Vec3f baseOffset() {
        //return new Vec3f(-5,-3,0);
        return new Vec3f(0, 0, 0);
    }

    public void renderButtons(ItemStack stack, MatrixStack matrixStack, MatrixStack otherPorjection, int background) {
        if (stack.getItem() instanceof ModularItem) {
            ModularItem.getModules(stack).allSubModules().forEach(moduleInstances -> {
                ModuleButton button = buttonMap.computeIfAbsent(moduleInstances, (moduleInstance) -> {
                    ModuleButton newButton = new ModuleButton(0, 0, 10, 10, moduleInstance);
                    addChild(newButton);
                    return newButton;
                });

                // Get the 3D position
                Vec3f position = SlotProperty.getTransform(moduleInstances).translation;

                float scaleAdjust = 3.8f;
                //float scaleAdjust = 1.0f;

                Vector4f pos = new Vector4f(((position.getX() + baseOffset().getX()) * scaleAdjust) / getSize(), ((position.getY() + baseOffset().getY()) * scaleAdjust) / getSize(), position.getZ() + baseOffset().getZ(), 1.0f);
                //matrixStack.peek().getPositionMatrix().multiply(otherPorjection.peek().getPositionMatrix());
                //otherPorjection.peek().getPositionMatrix().multiply(matrixStack.peek().getPositionMatrix());
                MatrixStack newStack = new MatrixStack();
                //newStack.scale(0.1f,0.1f,0.1f);
                newStack.multiplyPositionMatrix(matrixStack.peek().getPositionMatrix());
                //newStack.scale(0.1f,0.1f,0.1f);
                newStack.scale(getSize(), getSize(), getSize());
                pos.transform(newStack.peek().getPositionMatrix());
                //pos.transform(matrixStack.peek().getPositionMatrix());

                //matrixStack.peek().getPositionMatrix().

                // Project the position onto the screen space using the matrix stack
                //Vec3d projectedPos = matrixStack.peek().getPositionMatrix().
                //int screenX = (int) projectedPos.x;
                // int screenY = (int) projectedPos.y;

                // Update the position of the button
                int screenX = (int) ((pos.getX() / pos.getW() + 1) / 2.0f * MinecraftClient.getInstance().getWindow().getScaledWidth());
                int screenY = (int) ((1 - pos.getY() / pos.getW()) / 2.0f * MinecraftClient.getInstance().getWindow().getScaledHeight());
                Vec3f offest = position();
                button.x = (int) ((pos.getX() / pos.getW()) + offest.getX()) + button.getWidth() / 2;
                button.y = (int) ((pos.getY() / pos.getW()) + offest.getY()) + button.getHeight() / 2;
                //button.x = (int) pos.getX();
                //button.y = (int) pos.getY();
                //Miapi.LOGGER.error(" x "+button.x +" y "+ button.y+ " w " +pos.getW());
            });
        }
    }

    private double[] projectPoint(Matrix4f modelMatrix, double x, double y, double z) {
        Vector4f pos = new Vector4f((float) x, (float) y, (float) z, 1.0f);
        Vec3f pos2 = new Vec3f(0, 0, 0);
        pos.transform(modelMatrix);
        pos.transform(RenderSystem.getProjectionMatrix());
        return new double[]{pos.getX(), pos.getY()};
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {

    }

    public class ModuleButton extends InteractAbleWidget {
        private static final Identifier ButtonTexture = new Identifier(Miapi.MOD_ID, "textures/button.png");
        public ItemModule.ModuleInstance instance;

        public ModuleButton(int x, int y, int width, int height, ItemModule.ModuleInstance instance) {
            super(x, y, width, height, Text.literal(" "));
            this.instance = instance;
            Miapi.LOGGER.warn("new button");
        }

        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.depthFunc(GL11.GL_ALWAYS);
            RenderSystem.depthMask(false);
            this.renderButton(matrices, mouseX, mouseY, delta);
            RenderSystem.depthFunc(GL11.GL_ALWAYS);
            RenderSystem.depthMask(true);
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            setSelected(this.instance);
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

            if (selected == this.instance) {
                textureOffset = 20;
            } else if (this.isMouseOver(mouseX, mouseY)) {
                textureOffset = 10;
            }

            drawTexture(matrices, x, y, 0, textureOffset, 0, this.width, this.height, textureSize, 10);
        }
    }
}
