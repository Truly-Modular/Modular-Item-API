package smartin.miapi.client.model.module;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;
import smartin.miapi.client.model.MiapiModel;

@Environment(EnvType.CLIENT)
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
    public void render(RenderContext context) {
        age++;
        context.matrices().pushPose();
        context.matrices().translate(0.5, 0.5, 0.5);
        float scale = 1.0f / 16.0f;
        context.matrices().scale(scale, scale, scale);
        float h = 0.5f;
        float j = (age + context.tickDelta());
        VertexConsumer vertexConsumer = context.vertexConsumers().getBuffer(END_CRYSTAL);
        context.matrices().pushPose();
        context.matrices().scale(2.0F, 2.0F, 2.0F);
        context.matrices().translate(0.0F, -0.5F, 0.0F);
        int k = OverlayTexture.NO_OVERLAY;
        //if (endCrystalEntity.shouldShowBottom()) {
        //this.bottom.render(context.matrices(), vertexConsumer, i, k);
        //}

        context.matrices().mulPose(Axis.YP.rotationDegrees(j));
        context.matrices().translate(0.0F, 1.5F + h / 2.0F, 0.0F);
        context.matrices().mulPose((new Quaternionf()).setAngleAxis(1.0471976F, SINE_45_DEGREES, 0.0F, SINE_45_DEGREES));
        this.frame.render(context.matrices(), vertexConsumer, context.light(), k);
        float l = 0.875F;
        context.matrices().scale(0.875F, 0.875F, 0.875F);
        context.matrices().mulPose((new Quaternionf()).setAngleAxis(1.0471976F, SINE_45_DEGREES, 0.0F, SINE_45_DEGREES));
        context.matrices().mulPose(Axis.YP.rotationDegrees(j));
        this.frame.render(context.matrices(), vertexConsumer, context.light(), k);
        context.matrices().scale(0.875F, 0.875F, 0.875F);
        context.matrices().mulPose((new Quaternionf()).setAngleAxis(1.0471976F, SINE_45_DEGREES, 0.0F, SINE_45_DEGREES));
        context.matrices().mulPose(Axis.YP.rotationDegrees(j));
        this.core.render(context.matrices(), vertexConsumer, context.light(), k);
        context.matrices().popPose();
        context.matrices().popPose();
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
