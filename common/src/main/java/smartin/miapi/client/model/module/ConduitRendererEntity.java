package smartin.miapi.client.model.module;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.ConduitRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.item.modular.Transform;

import static net.minecraft.client.renderer.blockentity.ConduitRenderer.*;

@Environment(EnvType.CLIENT)
public class ConduitRendererEntity implements MiapiModel {
    Transform transform;
    ConduitRenderer renderer;
    ConduitBlockEntity conduitBlockEntity;
    private final ModelPart conduitEye;
    private final ModelPart conduitWind;
    private final ModelPart conduitShell;
    private final ModelPart conduit;

    public ConduitRendererEntity(Transform transform) {
        this.transform = transform;
        this.conduitBlockEntity = new ConduitBlockEntity(BlockPos.ZERO, Blocks.CONDUIT.defaultBlockState());
        this.conduitEye = getEyeTexturedModelData().bakeRoot();
        this.conduitWind = getWindTexturedModelData().bakeRoot();
        this.conduitShell = getShellTexturedModelData().bakeRoot();
        this.conduit = getPlainTexturedModelData().bakeRoot();
    }

    public static LayerDefinition getEyeTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        modelPartData.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 0.0F, new CubeDeformation(0.01F)), PartPose.ZERO);
        return LayerDefinition.create(modelData, 16, 16);
    }

    public static LayerDefinition getWindTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        modelPartData.addOrReplaceChild("wind", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F), PartPose.ZERO);
        return LayerDefinition.create(modelData, 64, 32);
    }

    public static LayerDefinition getShellTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        modelPartData.addOrReplaceChild("shell", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.ZERO);
        return LayerDefinition.create(modelData, 32, 16);
    }

    public static LayerDefinition getPlainTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        modelPartData.addOrReplaceChild("shell", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        return LayerDefinition.create(modelData, 32, 16);
    }

    @Override
    public void render(RenderContext context) {
        float f = 0;
        float h = conduitBlockEntity.getActiveRotation(f) * 57.295776F;
        float g = (float) conduitBlockEntity.tickCount + f;

        float k = Mth.sin(g * 0.1F) / 2.0F + 0.5F;
        k += k * k;
        context.matrices().pushPose();
        transform.applyPosition(context.matrices());
        context.matrices().pushPose();
        context.matrices().translate(0.5F, 0.3F + k * 0.2F, 0.5F);
        Vector3f vector3f = (new Vector3f(0.5F, 1.0F, 0.5F)).normalize();
        context.matrices().mulPose((new Quaternionf()).rotationAxis(h * 0.017453292F, vector3f));
        conduit.render(context.matrices(), ACTIVE_SHELL_TEXTURE.buffer(context.vertexConsumers(), RenderType::entityCutoutNoCull), context.light(),context.overlay());
        context.matrices().popPose();
        int l = conduitBlockEntity.tickCount / 66 % 3;
        context.matrices().pushPose();
        context.matrices().translate(0.5F, 0.5F, 0.5F);
        if (l == 1) {
            context.matrices().mulPose((new Quaternionf()).rotationX(1.5707964F));
        } else if (l == 2) {
            context.matrices().mulPose((new Quaternionf()).rotationZ(1.5707964F));
        }

        VertexConsumer vertexConsumer2 = (l == 1 ? VERTICAL_WIND_TEXTURE : WIND_TEXTURE).buffer(context.vertexConsumers(), RenderType::entityCutoutNoCull);
        this.conduitWind.render(context.matrices(), vertexConsumer2, context.light(),context.overlay());
        context.matrices().popPose();
        context.matrices().pushPose();
        context.matrices().translate(0.5F, 0.5F, 0.5F);
        context.matrices().scale(0.875F, 0.875F, 0.875F);
        context.matrices().mulPose((new Quaternionf()).rotationXYZ(3.1415927F, 0.0F, 3.1415927F));
        //this.conduitWind.render(context.matrices(), vertexConsumer2, i, j);
        context.matrices().popPose();
        //Camera camera = this.dispatcher.camera;
        context.matrices().pushPose();
        context.matrices().translate(0.5F, 0.3F + k * 0.2F, 0.5F);
        context.matrices().scale(0.5F, 0.5F, 0.5F);
        Matrix4f matrix4f = context.matrices().last().pose();
        float dir = matrix4f.getTranslation(new Vector3f()).x() > 0 ? 1 : -1;
        //dir = -1;
        matrix4f.rotate(matrix4f.getUnnormalizedRotation(new Quaternionf()).invert());
        matrix4f.rotate((float) (Math.PI), new Vector3f(1, 0, 0));
        //context.matrices().scale(dir, dir, dir);

        this.conduitEye.render(context.matrices(), (conduitBlockEntity.isHunting() ? OPEN_EYE_TEXTURE : CLOSED_EYE_TEXTURE).buffer(context.vertexConsumers(), RenderType::entityCutoutNoCull), LightTexture.FULL_BRIGHT, context.overlay());
        context.matrices().popPose();
        context.matrices().popPose();
    }
}