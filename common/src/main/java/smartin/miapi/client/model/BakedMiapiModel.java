package smartin.miapi.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import smartin.miapi.Miapi;
import smartin.miapi.client.renderer.TrimRenderer;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.render.ColorProperty;
import smartin.miapi.modules.properties.render.EmissivityProperty;
import smartin.miapi.modules.properties.GlintProperty;
import smartin.miapi.client.GlintShader;

public class BakedMiapiModel implements MiapiModel {
    ModuleInstance instance;
    BakedModel model;
    Matrix4f modelMatrix;
    ModelHolder modelHolder;
    RandomSource random = RandomSource.create();
    float[] colors;
    GlintProperty.GlintSettings settings;
    int skyLight;
    int blockLight;


    public BakedMiapiModel(ModelHolder holder, ModuleInstance moduleInstance, ItemStack stack) {
        this.modelHolder = holder;
        this.instance = holder.colorProvider().adapt(moduleInstance);
        Color color = holder.colorProvider().getVertexColor().orElse(ColorProperty.getColor(stack, instance));
        this.colors = new float[]{color.redAsFloat(), color.greenAsFloat(), color.blueAsFloat()};
        this.modelMatrix = holder.matrix4f();
        this.model = holder.model();
        settings = GlintProperty.property.getGlintSettings(instance, stack);

        skyLight = holder.lightValues()[0];
        blockLight = holder.lightValues()[1];

        int[] propertyLight = EmissivityProperty.getLightValues(instance);
        int propertySky = propertyLight[0];
        int propertyBlock = propertyLight[1];

        if (propertySky > skyLight) skyLight = propertySky;
        if (propertyBlock > blockLight) blockLight = propertyBlock;
    }

    @Override
    public void render(PoseStack matrices, ItemStack stack, ItemDisplayContext transformationMode, float tickDelta, MultiBufferSource vertexConsumers, LivingEntity entity, int packedLight, int overlay) {
        assert Minecraft.getInstance().level != null;
        matrices.pushPose();

        int sky = LightTexture.sky(packedLight);
        int block = LightTexture.block(packedLight);

        if (skyLight > sky) sky = skyLight;
        if (blockLight > block) block = blockLight;

        int light = LightTexture.pack(block, sky);

        Transform.applyPosition(matrices, modelMatrix);
        BakedModel currentModel = resolve(model, stack, entity, light);
        Minecraft.getInstance().level.getProfiler().push("BakedModel");

        //render normally
        try {
            for (Direction dir : Direction.values()) {
                currentModel.getQuads(null, dir, RandomSource.create()).forEach(quad -> {
                    VertexConsumer vertexConsumer = modelHolder.colorProvider().getConsumer(vertexConsumers, quad.getSprite(), stack, instance, transformationMode);

                    vertexConsumer.putBulkData(matrices.last(), quad, colors[0], colors[1], colors[2], 1.0f, light, overlay);
                });
            }
        } catch (RuntimeException e) {
            Miapi.LOGGER.error("rendering error in module " + instance.moduleID + " " + MaterialProperty.getMaterial(instance), e);
        }
        Minecraft.getInstance().level.getProfiler().pop();

        Minecraft.getInstance().level.getProfiler().push("BakedModel Glint");

        //render normally
        if (stack.hasFoil() && MiapiConfig.INSTANCE.client.enchantingGlint.enabled) {
            try {
                VertexConsumer altConsumer = vertexConsumers.getBuffer(GlintShader.modularItemGlint);
                for (Direction dir : Direction.values()) {
                    currentModel.getQuads(null, dir, RandomSource.create()).forEach(quad -> {
                        Color glintColor = settings.getColor();
                        altConsumer.putBulkData(matrices.last(), quad, glintColor.redAsFloat(), glintColor.greenAsFloat(), glintColor.blueAsFloat(), 1.0f, light, overlay);

                    });
                }
            } catch (RuntimeException e) {
                Miapi.LOGGER.error("rendering glint error in module " + instance.moduleID + " " + MaterialProperty.getMaterial(instance), e);
            }
        }
        Minecraft.getInstance().level.getProfiler().pop();

        Minecraft.getInstance().level.getProfiler().push("TrimModel");
        //render Trims
        Holder<ArmorMaterial> armorMaterial = (stack.getItem() instanceof ArmorItem armorItem) ? armorItem.getMaterial() : null;

        if (armorMaterial != null && !modelHolder.trimMode().equals(TrimRenderer.TrimMode.NONE)) {
            ModelTransformer.getRescale(currentModel, random).forEach(quad -> {
                TrimRenderer.renderTrims(matrices, quad, modelHolder.trimMode(), light, vertexConsumers, armorMaterial, stack);
            });
        }
        Minecraft.getInstance().level.getProfiler().pop();

        //render from both sides if requested
        if (modelHolder.entityRendering()) {
            Minecraft.getInstance().level.getProfiler().push("EntityModel");
            ModelTransformer.getInverse(currentModel, random).forEach(quad -> {
                VertexConsumer vertexConsumer = modelHolder.colorProvider().getConsumer(vertexConsumers, quad.getSprite(), stack, instance, transformationMode);
                vertexConsumer.putBulkData(matrices.last(), quad, colors[0], colors[1], colors[2], 1.0f, light, overlay);
            });
            Minecraft.getInstance().level.getProfiler().pop();
        }
        matrices.popPose();
    }

    public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable LivingEntity entity, int light) {
        if (model.getOverrides() != null && !model.getOverrides().equals(ItemOverrides.EMPTY)) {
            BakedModel override = model.getOverrides().resolve(model, stack, Minecraft.getInstance().level, entity, light);
            if (model != null) {
                model = override;
            }
        }
        return model;
    }
}
