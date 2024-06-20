package smartin.miapi.client.model;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ConduitBlockEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.ConduitBlockEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import smartin.miapi.item.modular.Transform;

import static net.minecraft.client.render.block.entity.ConduitBlockEntityRenderer.*;

public class ConduitRendererEntity implements MiapiModel {
    Transform transform;
    ConduitBlockEntityRenderer renderer;
    ConduitBlockEntity conduitBlockEntity;
    private final ModelPart conduitEye;
    private final ModelPart conduitWind;
    private final ModelPart conduitShell;
    private final ModelPart conduit;

    public ConduitRendererEntity(Transform transform) {
        this.transform = transform;
        this.conduitBlockEntity = new ConduitBlockEntity(BlockPos.ORIGIN, Blocks.CONDUIT.getDefaultState());
        this.conduitEye = getEyeTexturedModelData().createModel();
        this.conduitWind = getWindTexturedModelData().createModel();
        this.conduitShell = getShellTexturedModelData().createModel();
        this.conduit = getPlainTexturedModelData().createModel();
    }

    public static TexturedModelData getEyeTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("eye", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 0.0F, new Dilation(0.01F)), ModelTransform.NONE);
        return TexturedModelData.of(modelData, 16, 16);
    }

    public static TexturedModelData getWindTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("wind", ModelPartBuilder.create().uv(0, 0).cuboid(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F), ModelTransform.NONE);
        return TexturedModelData.of(modelData, 64, 32);
    }

    public static TexturedModelData getShellTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("shell", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), ModelTransform.NONE);
        return TexturedModelData.of(modelData, 32, 16);
    }

    public static TexturedModelData getPlainTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("shell", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), ModelTransform.NONE);
        return TexturedModelData.of(modelData, 32, 16);
    }

    @Override
    public void render(MatrixStack matrices, ItemStack stack, ModelTransformationMode transformationMode, float f, VertexConsumerProvider vertexConsumers, LivingEntity entity, int light, int overlay) {
        f = 0;
        float h = conduitBlockEntity.getRotation(f) * 57.295776F;
        float g = (float) conduitBlockEntity.ticks + f;

        float k = MathHelper.sin(g * 0.1F) / 2.0F + 0.5F;
        k += k * k;
        matrices.push();
        transform.applyPosition(matrices);
        matrices.push();
        matrices.translate(0.5F, 0.3F + k * 0.2F, 0.5F);
        Vector3f vector3f = (new Vector3f(0.5F, 1.0F, 0.5F)).normalize();
        matrices.multiply((new Quaternionf()).rotationAxis(h * 0.017453292F, vector3f));
        conduit.render(matrices, CAGE_TEXTURE.getVertexConsumer(vertexConsumers, RenderLayer::getEntityCutoutNoCull), light, overlay);
        matrices.pop();
        int l = conduitBlockEntity.ticks / 66 % 3;
        matrices.push();
        matrices.translate(0.5F, 0.5F, 0.5F);
        if (l == 1) {
            matrices.multiply((new Quaternionf()).rotationX(1.5707964F));
        } else if (l == 2) {
            matrices.multiply((new Quaternionf()).rotationZ(1.5707964F));
        }

        VertexConsumer vertexConsumer2 = (l == 1 ? WIND_VERTICAL_TEXTURE : WIND_TEXTURE).getVertexConsumer(vertexConsumers, RenderLayer::getEntityCutoutNoCull);
        this.conduitWind.render(matrices, vertexConsumer2, light, overlay);
        matrices.pop();
        matrices.push();
        matrices.translate(0.5F, 0.5F, 0.5F);
        matrices.scale(0.875F, 0.875F, 0.875F);
        matrices.multiply((new Quaternionf()).rotationXYZ(3.1415927F, 0.0F, 3.1415927F));
        //this.conduitWind.render(matrices, vertexConsumer2, i, j);
        matrices.pop();
        //Camera camera = this.dispatcher.camera;
        matrices.push();
        matrices.translate(0.5F, 0.3F + k * 0.2F, 0.5F);
        matrices.scale(0.5F, 0.5F, 0.5F);
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float dir = matrix4f.getTranslation(new Vector3f()).x() > 0 ? 1 : -1;
        //dir = -1;
        matrix4f.rotate(matrix4f.getUnnormalizedRotation(new Quaternionf()).invert());
        matrix4f.rotate((float) (Math.PI), new Vector3f(1, 0, 0));
        //matrices.scale(dir, dir, dir);

        this.conduitEye.render(matrices, (conduitBlockEntity.isEyeOpen() ? OPEN_EYE_TEXTURE : CLOSED_EYE_TEXTURE).getVertexConsumer(vertexConsumers, RenderLayer::getEntityCutoutNoCull), LightmapTextureManager.MAX_LIGHT_COORDINATE, overlay);
        matrices.pop();
        matrices.pop();
    }
}