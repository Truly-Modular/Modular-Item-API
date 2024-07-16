package smartin.miapi.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

public class CrystalModel implements MiapiModel {
    LayerDefinition modelData;
    ResourceLocation TEXTURE = ResourceLocation.parse("textures/entity/end_crystal/end_crystal.png");
    RenderType END_CRYSTAL = RenderType.entityCutoutNoCull(TEXTURE);
    int age;
    float SINE_45_DEGREES = (float) Math.sin(0.7853981633974483);
    ModelPart core;
    ModelPart frame;
    ModelPart bottom;

    public CrystalModel() {
        modelData = getTexturedModelData();
        ModelPart modelPart = modelData.bakeRoot();
        this.frame = modelPart.getChild("glass");
        this.core = modelPart.getChild("cube");
        this.bottom = modelPart.getChild("base");
        EndCrystalRenderer renderer;
    }

    @Override
    public void render(PoseStack matrixStack, ItemStack stack, ItemDisplayContext transformationMode, float tickDelta, MultiBufferSource vertexConsumers, LivingEntity entity, int light, int overlay) {
        age++;
        matrixStack.pushPose();
        matrixStack.translate(0.5, 0.5, 0.5);
        float scale = 1.0f / 16.0f;
        matrixStack.scale(scale, scale, scale);
        float h = 0.5f;
        float j = (age + tickDelta);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(END_CRYSTAL);
        matrixStack.pushPose();
        matrixStack.scale(2.0F, 2.0F, 2.0F);
        matrixStack.translate(0.0F, -0.5F, 0.0F);
        int k = OverlayTexture.NO_OVERLAY;
        //if (endCrystalEntity.shouldShowBottom()) {
        //this.bottom.render(matrixStack, vertexConsumer, i, k);
        //}

        matrixStack.mulPose(Axis.YP.rotationDegrees(j));
        matrixStack.translate(0.0F, 1.5F + h / 2.0F, 0.0F);
        matrixStack.mulPose((new Quaternionf()).setAngleAxis(1.0471976F, SINE_45_DEGREES, 0.0F, SINE_45_DEGREES));
        this.frame.render(matrixStack, vertexConsumer, light, k);
        float l = 0.875F;
        matrixStack.scale(0.875F, 0.875F, 0.875F);
        matrixStack.mulPose((new Quaternionf()).setAngleAxis(1.0471976F, SINE_45_DEGREES, 0.0F, SINE_45_DEGREES));
        matrixStack.mulPose(Axis.YP.rotationDegrees(j));
        this.frame.render(matrixStack, vertexConsumer, light, k);
        matrixStack.scale(0.875F, 0.875F, 0.875F);
        matrixStack.mulPose((new Quaternionf()).setAngleAxis(1.0471976F, SINE_45_DEGREES, 0.0F, SINE_45_DEGREES));
        matrixStack.mulPose(Axis.YP.rotationDegrees(j));
        this.core.render(matrixStack, vertexConsumer, light, k);
        matrixStack.popPose();
        matrixStack.popPose();
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        modelPartData.addOrReplaceChild("glass", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        modelPartData.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        modelPartData.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 16).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 4.0F, 12.0F), PartPose.ZERO);
        return LayerDefinition.create(modelData, 64, 32);
    }
}
