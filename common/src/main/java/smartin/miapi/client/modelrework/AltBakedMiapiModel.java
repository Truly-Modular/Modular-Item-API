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
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;
import smartin.miapi.Miapi;
import smartin.miapi.client.AltModelAtlasManager;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;

import java.lang.ref.WeakReference;
import java.util.*;

public class AltBakedMiapiModel implements MiapiModel {
    ItemModule.ModuleInstance instance;
    Material material;
    BakedModel model;
    Matrix4f modelMatrix;
    Color color;
    BakedMiapiModel.ModelHolder modelHolder;
    Random random = Random.create();
    float[] colors;
    Sprite textureSprite = null;
    Map<Sprite, Identifier> replaceSprites = new HashMap<>();

    public Map<BakedModel, List<BakedQuad>> quadLookupMap = new HashMap<>();

    public AltBakedMiapiModel(BakedMiapiModel.ModelHolder holder, ItemModule.ModuleInstance instance, ItemStack stack) {
        modelHolder = holder;
        this.instance = instance;
        material = MaterialProperty.getMaterial(instance);
        color = holder.colorProvider().getVertexColor();
        modelMatrix = holder.matrix4f();
        model = holder.model();
        colors = new float[]{color.redAsFloat(), color.greenAsFloat(), color.blueAsFloat()};
        List<BakedQuad> quadsList = holder.model().getQuads(null, Direction.DOWN, random);
        if (quadsList.size() > 0) {
            textureSprite = holder.model().getQuads(null, Direction.DOWN, random).get(0).getSprite();
            SpriteContents contents = holder.colorProvider().tranform(textureSprite.getContents());
            String id = textureSprite.getContents().getId().getPath() + "_no_material";
            if (material != null) {
                id = textureSprite.getContents().getId().getPath() + "_" + material.getKey();
            }
            id = "materialsprite_testing";
            textureSprite = new Sprite(new Identifier(Miapi.MOD_ID, id), contents, contents.getWidth(), contents.getHeight(), contents.getWidth(), contents.getHeight());
            ResourceManager manager = MinecraftClient.getInstance().getResourceManager();
            //manager.getResource(id);
            //textureSprite.upload();
            BakedQuad quad;
        }
        AltModelAtlasManager.models.add(new WeakReference<>(this));
        AltModelAtlasManager.atlasInstance.update();
        //AltModelAtlasManager.atlasInstance.update();
    }

    public List<AltModelAtlasManager.SpriteInfoHolder> getSprites() {
        List<AltModelAtlasManager.SpriteInfoHolder> spriteInfoHolders = new ArrayList<>();
        List<BakedQuad> quadsList = modelHolder.model().getQuads(null, Direction.DOWN, random);
        if (quadsList.size() > 0) {
            textureSprite = modelHolder.model().getQuads(null, Direction.DOWN, random).get(0).getSprite();
            //SpriteContents contents = textureSprite.getContents();//
            SpriteContents contents = modelHolder.colorProvider().tranform(textureSprite.getContents());
            spriteInfoHolders.add(new AltModelAtlasManager.SpriteInfoHolder(contents, material));
            replaceSprites.put(textureSprite, contents.getId());
        }
        return spriteInfoHolders;
    }

    @Override
    public void render(MatrixStack matrices, ItemStack stack, ModelTransformationMode transformationMode, float tickDelta, VertexConsumerProvider vertexConsumers, LivingEntity entity, int light, int overlay) {
        assert MinecraftClient.getInstance().world != null;
        if (textureSprite == null) return;
        MinecraftClient.getInstance().world.getProfiler().push("BakedModel");
        matrices.push();
        matrices.multiplyPositionMatrix(modelMatrix);
        BakedModel currentModel = model;
        if (model.getOverrides() != null && !model.getOverrides().equals(ModelOverrideList.EMPTY)) {
            currentModel = model.getOverrides().apply(model, stack, MinecraftClient.getInstance().world, entity, light);
        }
        MinecraftClient.getInstance().world.getProfiler().push("QuadPushing");
        //VertexConsumer consumer = modelHolder.colorProvider.getConsumer(vertexConsumers, stack, instance, transformationMode);
        RenderLayer renderLayer = RenderLayers.getItemLayer(stack, true);
        //textureSprite.upload();
        renderLayer = RenderLayer.getEntityTranslucentCull(AltModelAtlasManager.MATERIAL_ATLAS_ID);
        VertexConsumer consumerOld = ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, renderLayer, true, stack.hasGlint());
        Sprite replaceSprite = AltModelAtlasManager.atlasInstance.getSprite(replaceSprites.get(textureSprite));
        if (replaceSprite == null) {
            MinecraftClient.getInstance().world.getProfiler().pop();
            MinecraftClient.getInstance().world.getProfiler().pop();
            matrices.pop();
            return;
        }
        VertexConsumer consumer = replaceSprite.getTextureSpecificVertexConsumer(consumerOld);

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
                bakedQuad.getVertexData();
                int[] newVertexData = bakedQuad.getVertexData();
                for (int i = 0; i < bakedQuad.getVertexData().length / 8; i++) {
                    newVertexData[i * 8 + 4] = Float.floatToRawIntBits((Float.intBitsToFloat(newVertexData[i * 8 + 4]) - uStart) * uScale);
                    newVertexData[i * 8 + 5] = Float.floatToRawIntBits((Float.intBitsToFloat(newVertexData[i * 8 + 5]) - vStart) * vScale);
                }
                redoneQuads.add(new BakedQuad(bakedQuad.getVertexData(), bakedQuad.getColorIndex(), bakedQuad.getFace(), bakedQuad.getSprite(), bakedQuad.hasShade()));
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
        }).forEach(bakedQuad -> consumer.quad(matrices.peek(), bakedQuad, colors[0], colors[1], colors[2], light, overlay));
        for (Direction direction : Direction.values()) {
            currentModel.getQuads(null, direction, random)
                    .forEach(bakedQuad -> consumer.quad(matrices.peek(), bakedQuad, colors[0], colors[1], colors[2], light, overlay));
        }

        MinecraftClient.getInstance().world.getProfiler().pop();
        MinecraftClient.getInstance().world.getProfiler().pop();
        matrices.pop();
    }
}
