package smartin.miapi.client.modelrework;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.render.colorproviders.ColorProvider;

public class Alt2BakedMiapiModel implements MiapiModel {
    ModuleInstance instance;
    Material material;
    BakedModel model;
    Matrix4f modelMatrix;
    Color color;
    BakedMiapiModel.ModelHolder modelHolder;
    Random random = Random.create();
    float[] colors;

    public Alt2BakedMiapiModel(BakedMiapiModel.ModelHolder holder, ModuleInstance instance, ItemStack stack) {
        modelHolder = holder;
        this.instance = instance;
        material = MaterialProperty.getMaterial(instance);
        if (holder.colorProvider() instanceof ColorProvider.MaterialColorProvider colorProvider) {
            material = colorProvider.material;
        }
        color = holder.colorProvider().getVertexColor();
        modelMatrix = holder.matrix4f();
        model = holder.model();
        colors = new float[]{color.redAsFloat(), color.greenAsFloat(), color.blueAsFloat()};

        //AltModelAtlasManager.models.add(new WeakReference<>(new AltBakedMiapiModel(holder, instance, stack)));
        //AltModelAtlasManager.shouldUpdate = true;
    }

                /*
            0: X position
            1: Y position
            2: Z position
            3: Color (assumed to be stored as a packed integer)
            4: Texture U coordinate
            5: Texture V coordinate
            6: Lighting value (assumed to be stored as a packed integer)
            7: Normals (assumed to be stored as a packed integer)
             */

    @Override
    public void render(MatrixStack matrices, ItemStack stack, ModelTransformationMode transformationMode, float tickDelta, VertexConsumerProvider vertexConsumers, LivingEntity entity, int light, int overlay) {
        assert MinecraftClient.getInstance().world != null;
        matrices.push();
        Transform.applyPosition(matrices, modelMatrix);
        BakedModel currentModel = model;
        if (model.getOverrides() != null && !model.getOverrides().equals(ModelOverrideList.EMPTY)) {
            currentModel = model.getOverrides().apply(model, stack, MinecraftClient.getInstance().world, entity, light);
        }
        if (!(modelHolder.colorProvider() instanceof ColorProvider.MaterialColorProvider) || material == null) {
            badShaderRenderer(matrices, stack, currentModel, vertexConsumers, light, overlay);
            matrices.pop();
            return;
        }
        MinecraftClient.getInstance().world.getProfiler().push("BakedModel");
        assert currentModel != null;
        ArmorTrim trim = ArmorTrim.getTrim(entity.getWorld().getRegistryManager(), stack).orElse(null);
        ArmorMaterial armorMaterial;
        if (stack.getItem() instanceof ArmorItem armorItem) {
            armorMaterial = armorItem.getMaterial();
        } else {
            armorMaterial = null;
        }
        ModelTransformer.getRescale(currentModel, random).forEach(bakedQuad -> {
            Identifier replaceId = MaterialSpriteManager.getMaterialSprite(bakedQuad.getSprite(), material);
            if (replaceId != null) {
                RenderLayer atlasRenderLayer = RenderLayer.getEntityTranslucentCull(replaceId);
                //VertexConsumer atlasConsumer = ItemRenderer.getDirectDynamicDisplayGlintConsumer(vertexConsumers,atlasRenderLayer,matrices.peek());
                VertexConsumer atlasConsumer = ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, atlasRenderLayer, true, false);
                atlasConsumer.quad(matrices.peek(), bakedQuad, colors[0], colors[1], colors[2], light, overlay);


                if (trim != null) {
                    if (armorMaterial != null) {
                        if (!modelHolder.trimMode().equals(TrimRenderer.TrimMode.NONE)) {
                            TrimRenderer.renderTrims(matrices, bakedQuad, modelHolder.trimMode(), light, vertexConsumers, armorMaterial, stack);

                        }
                    }
                }
            }
        });

        if (modelHolder.entityRendering()) {
            ModelTransformer.getRescaleInverse(currentModel, random).forEach(bakedQuad -> {
                Identifier replaceId = MaterialSpriteManager.getMaterialSprite(bakedQuad.getSprite(), material);
                if (replaceId != null) {
                    RenderLayer atlasRenderLayer = RenderLayer.getEntityTranslucentCull(replaceId);
                    VertexConsumer atlasConsumer = ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, atlasRenderLayer, true, false);
                    //atlasConsumer = vertexConsumers.getBuffer(atlasRenderLayer);
                    atlasConsumer.quad(matrices.peek(), bakedQuad, colors[0], colors[1], colors[2], light, overlay);
                }
            });
        }
        MinecraftClient.getInstance().world.getProfiler().pop();
        matrices.pop();
    }

    public void badShaderRenderer(MatrixStack matrices, ItemStack stack, BakedModel currentModel, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        RenderLayer renderLayer = RenderLayers.getItemLayer(stack, true);
        VertexConsumer consumer = ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, renderLayer, true, false);
        Color materialColor;
        if (material != null && modelHolder.colorProvider() instanceof ColorProvider.MaterialColorProvider) {
            materialColor = new Color(material.getColor());
        } else {
            materialColor = modelHolder.colorProvider().getVertexColor();
        }
        for (Direction direction : Direction.values()) {
            currentModel.getQuads(null, direction, random).forEach(bakedQuad -> {
                consumer.quad(matrices.peek(), bakedQuad, materialColor.redAsFloat(), materialColor.greenAsFloat(), materialColor.blueAsFloat(), light, overlay);
            });
        }
    }
}
