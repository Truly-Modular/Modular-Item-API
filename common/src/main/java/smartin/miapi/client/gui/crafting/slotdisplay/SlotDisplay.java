package smartin.miapi.client.gui.crafting.slotdisplay;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.client.model.ModuleModel;
import smartin.miapi.client.model.collision.Ray;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.slot.SlotProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Provides a 3D interactive view of a modular item.
 */
@Environment(EnvType.CLIENT)
public class SlotDisplay extends InteractAbleWidget {

    private final Map<SlotProperty.ModuleSlot, ModuleButton> buttonMap = new HashMap<>();

    private ItemStack stack;

    private PoseStack slotProjection = new PoseStack();

    private double lastMouseX;
    private double lastMouseY;

    private boolean mouseDown0 = false;
    private boolean mouseDown1 = false;

    private SlotProperty.ModuleSlot selected = null;
    private final Consumer<SlotProperty.ModuleSlot> setSelected;

    private SlotProperty.ModuleSlot baseSlot;

    public SlotDisplay(ItemStack stack, int x, int y, int height, int width, Consumer<SlotProperty.ModuleSlot> selected) {
        super(x, y, width, height, Component.literal("Item Display"));

        this.stack = stack;
        this.height = height;
        this.width = width;
        this.setSelected = selected;

        slotProjection.scale(1.0F, -1.0F, 1.0F);

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
            return false;
        }

        return true;
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
        PoseStack newStack = new PoseStack();

        newStack.translate((float) -deltaX / 100.0F, (float) -deltaY / 100.0F, 0.0F);

        newStack.mulPose(slotProjection.last().pose());

        slotProjection = newStack;
    }

    private void handleRightClickDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        float angleX = (float) -(deltaY * 0.02F);

        float angleY = (float) -(deltaX * 0.02F);

        PoseStack newStack = new PoseStack();

        newStack.last().pose().rotateAffineXYZ(-angleX, angleY, 0.0F);

        newStack.mulPose(slotProjection.last().pose());

        slotProjection = newStack;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isMouseOver(mouseX, mouseY)) {
            double scale = Math.pow(2.0D, scrollY / 10.0D);

            slotProjection.scale((float) scale, (float) scale, (float) scale);

            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public void setBaseSlot(SlotProperty.ModuleSlot baseSlot1) {
        baseSlot = baseSlot1;

        buttonMap.forEach((slot, button) -> children().remove(button));

        buttonMap.clear();

        if (baseSlot == null || baseSlot.inSlot == null) {
            return;
        }

        baseSlot.inSlot.cache().getSortedChildren().forEach(moduleInstance -> SlotProperty.getSlots(moduleInstance).forEach((number, slot) -> buttonMap.computeIfAbsent(slot, newSlot -> {
            ModuleButton button = new ModuleButton(0, 0, 10, 10, newSlot);
            addChild(button);
            return button;
        })));

        buttonMap.computeIfAbsent(baseSlot, newSlot -> {
            ModuleButton button = new ModuleButton(0, 0, 10, 10, newSlot);
            addChild(button);
            return button;
        });
    }

    public void setItem(ItemStack itemStack) {
        stack = itemStack;
        slotProjection = new PoseStack();
        slotProjection.scale(1.0F, -1.0F, 1.0F);
        selected = new SlotProperty.ModuleSlot(new ArrayList<>());
    }

    public int getSize() {
        int size = Math.min(width, height);
        size = Math.max(5, size - 10);
        return size;
    }

    @Override
    public void renderWidget(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        drawContext.enableScissor(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height);
        renderSlot(stack, drawContext, mouseX, mouseY, delta);
        super.renderWidget(drawContext, mouseX, mouseY, delta);
        drawContext.disableScissor();
    }

    public void select(SlotProperty.ModuleSlot selected) {
        this.selected = selected;
    }

    private Vector3f position() {
        return new Vector3f(getX() + (float) (width - 16) / 2.0F, getY() + (float) (height - 16) / 2.0F, 150.0F);
    }

    public void renderSlot(ItemStack stack, GuiGraphics context, int mouseX, int mouseY, float delta) {
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);

        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        var matrixStack = RenderSystem.getModelViewStack();
        matrixStack.pushMatrix();
        Vector3f pos = position();

        matrixStack.translate(pos.x(), pos.y(), pos.z());
        float size = getSize();
        matrixStack.scale(size, size, 1.0F);

        RenderSystem.applyModelViewMatrix();
        MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();
        Lighting.setupForFlatItems();
        RenderSystem.enableDepthTest();

        if (!Screen.hasAltDown()) {
            renderer.renderStatic(stack, ItemDisplayContext.GUI, 15728880, OverlayTexture.NO_OVERLAY, slotProjection, immediate, Minecraft.getInstance().level, 0);
        }
        updateHoveredModule(mouseX, mouseY, delta, Screen.hasAltDown() ? immediate : null);

        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();
        matrixStack.popMatrix();
        immediate.endBatch();
        RenderSystem.applyModelViewMatrix();
    }

    private void updateHoveredModule(int mouseX, int mouseY, float delta, MultiBufferSource multiBufferSource) {
        if (!isMouseOver(mouseX, mouseY)) {
            setHoveredModule(null);
            return;
        }

        if (!ModularItem.isModularItem(stack)) {
            return;
        }
        MiapiItemModel itemModel = MiapiItemModel.getItemModel(stack);

        if (itemModel == null) {
            setHoveredModule(null);
            return;
        }

        Ray ray = createMouseRay(mouseX, mouseY);

        Matrix4f modelToRaySpace = new Matrix4f(slotProjection.last().pose());

        Optional<ModuleModel.ModuleRayHit> result = itemModel.raycast(null, multiBufferSource, stack, ItemDisplayContext.GUI, delta, ray, modelToRaySpace);

        if (result.isEmpty()) {
            setHoveredModule(null);
            return;
        }
        ModuleModel.ModuleRayHit hit = result.get();
        setHoveredModule(hit.module());
    }

    private Ray createMouseRay(double mouseX, double mouseY) {
        Vector3f pos = position();
        float size = getSize();

        float x = (float) ((mouseX - pos.x()) / size);
        float y = (float) ((mouseY - pos.y()) / size);

        Vector3f origin = new Vector3f(x, y, 10.0F);
        Vector3f direction = new Vector3f(0.0F, 0.0F, -1.0F);

        return new Ray(origin, direction);
    }

    private void setHoveredModule(ModuleInstance module) {
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    public class ModuleButton extends InteractAbleWidget {

        private static final ResourceLocation ButtonTexture = ResourceLocation.fromNamespaceAndPath(Miapi.MOD_ID, "textures/button.png");

        public SlotProperty.ModuleSlot instance;

        public ModuleButton(int x, int y, int width, int height, SlotProperty.ModuleSlot instance) {
            super(x, y, width, height, Component.literal(" "));
            this.instance = instance;
        }

        private void setSelected(SlotProperty.ModuleSlot instance) {
            selected = instance;
            setSelected.accept(instance);
        }

        @Override
        public void renderWidget(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
            RenderSystem.depthFunc(GL11.GL_ALWAYS);
            RenderSystem.disableDepthTest();
            renderButton(drawContext, mouseX, mouseY, delta);
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.isMouseOver(mouseX, mouseY)) {
                playClickedSound();
                setSelected(this.instance);
                return true;
            }

            return super.mouseClicked(mouseX, mouseY, button);
        }

        public void renderButton(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);

            int textureSize = 30;
            int textureOffset = 0;

            if (this.instance.equals(selected)) {
                textureOffset = 20;
            } else if (this.isMouseOver(mouseX, mouseY)) {
                textureOffset = 10;
            }

            /*
             * Enable this once the 3D module positions are projected
             * into these buttons.
             */
            // drawTexture(
            //         drawContext,
            //         ButtonTexture,
            //         getX(),
            //         getY(),
            //         0,
            //         textureOffset,
            //         0,
            //         this.width,
            //         this.height,
            //         textureSize,
            //         10
            // );
        }
    }
}