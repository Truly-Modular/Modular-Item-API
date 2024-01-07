package smartin.miapi.client.modelrework;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;
import smartin.miapi.client.AltModelAtlasManager;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.render.colorproviders.ColorProvider;

import java.util.*;

public class Alt2BakedMiapiModel implements MiapiModel {
    ItemModule.ModuleInstance instance;
    Material material;
    BakedModel model;
    Matrix4f modelMatrix;
    Color color;
    BakedMiapiModel.ModelHolder modelHolder;
    Random random = Random.create();
    float[] colors;
    Map<Identifier, Identifier> replaceSprites = new HashMap<>();
    public boolean uploaded = false;
    Set<AltModelAtlasManager.SpriteInfoHolder> spriteInfos;

    public Map<BakedModel, List<BakedQuad>> quadLookupMap = new HashMap<>();

    public Alt2BakedMiapiModel(BakedMiapiModel.ModelHolder holder, ItemModule.ModuleInstance instance, ItemStack stack) {
        modelHolder = holder;
        this.instance = instance;
        material = MaterialProperty.getMaterial(instance);
        if(holder.colorProvider() instanceof ColorProvider.MaterialColorProvider colorProvider){
            material = colorProvider.material;
        }
        color = holder.colorProvider().getVertexColor();
        modelMatrix = holder.matrix4f();
        model = holder.model();
        colors = new float[]{color.redAsFloat(), color.greenAsFloat(), color.blueAsFloat()};

        //AltModelAtlasManager.models.add(new WeakReference<>(new AltBakedMiapiModel(holder, instance, stack)));
        //AltModelAtlasManager.shouldUpdate = true;
    }

    @Override
    public void render(MatrixStack matrices, ItemStack stack, ModelTransformationMode transformationMode, float tickDelta, VertexConsumerProvider vertexConsumers, LivingEntity entity, int light, int overlay) {
        assert MinecraftClient.getInstance().world != null;
        matrices.push();
        matrices.multiplyPositionMatrix(modelMatrix);
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
        quadLookupMap.computeIfAbsent(currentModel, model -> {
            List<BakedQuad> rawQuads = new ArrayList<>();
            for (Direction direction : Direction.values()) {
                rawQuads.addAll(model.getQuads(null, direction, random));
            }
            List<BakedQuad> redoneQuads = new ArrayList<>();
            rawQuads.forEach(bakedQuad -> {
                float uStart = bakedQuad.getSprite().getMinU();
                float uScale = 1 / (bakedQuad.getSprite().getMaxU() - bakedQuad.getSprite().getMinU());
                float vStart = bakedQuad.getSprite().getMinV();
                float vScale = 1 / (bakedQuad.getSprite().getMaxV() - bakedQuad.getSprite().getMinV());
                int[] copiedArray = new int[bakedQuad.getVertexData().length];

                System.arraycopy(bakedQuad.getVertexData(), 0, copiedArray, 0, bakedQuad.getVertexData().length);
                for (int i = 0; i < bakedQuad.getVertexData().length / 8; i++) {
                    copiedArray[i * 8 + 4] = Float.floatToRawIntBits((Float.intBitsToFloat(copiedArray[i * 8 + 4]) - uStart) * uScale);
                    copiedArray[i * 8 + 5] = Float.floatToRawIntBits((Float.intBitsToFloat(copiedArray[i * 8 + 5]) - vStart) * vScale);
                }
                redoneQuads.add(new BakedQuad(copiedArray, bakedQuad.getColorIndex(), bakedQuad.getFace(), bakedQuad.getSprite(), bakedQuad.hasShade()));
            });
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

            return redoneQuads;
        }).forEach(bakedQuad -> {
            Identifier replaceId = MaterialSpriteManager.getMaterialSprite(bakedQuad.getSprite(), material);
            if (replaceId != null) {
                RenderLayer atlasRenderLayer = RenderLayer.getEntityTranslucentCull(replaceId);
                VertexConsumer atlasConsumer = ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, atlasRenderLayer, true, false);
                atlasConsumer.quad(matrices.peek(), bakedQuad, colors[0], colors[1], colors[2], light, overlay);
            }
        });
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
