package smartin.miapi.client.modelrework;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.render.colorproviders.ColorProvider;

public class BakedMiapiModel implements MiapiModel {
    ModuleInstance instance;
    Material material;
    BakedModel model;
    Matrix4f modelMatrix;
    Color color;
    ModelHolder modelHolder;
    Random random = Random.create();
    float[] colors;
    private final SpriteAtlasTexture armorTrimsAtlas = MinecraftClient.getInstance().getBakedModelManager().getAtlas(TexturedRenderLayers.ARMOR_TRIMS_ATLAS_TEXTURE);

    public BakedMiapiModel(ModelHolder holder, ModuleInstance instance, ItemStack stack) {
        modelHolder = holder;
        this.instance = instance;
        material = MaterialProperty.getMaterial(instance);
        color = holder.colorProvider.getVertexColor();
        modelMatrix = holder.matrix4f();
        model = holder.model();
        colors = new float[]{color.redAsFloat(), color.greenAsFloat(), color.blueAsFloat()};
    }

    @Override
    public void render(MatrixStack matrices, ItemStack stack, ModelTransformationMode transformationMode, float tickDelta, VertexConsumerProvider vertexConsumers, LivingEntity entity, int light, int overlay) {
        assert MinecraftClient.getInstance().world != null;
        MinecraftClient.getInstance().world.getProfiler().push("BakedModel");
        matrices.push();
        Transform.applyPosition(matrices, modelMatrix);
        BakedModel currentModel = model;
        if (model.getOverrides() != null && !model.getOverrides().equals(ModelOverrideList.EMPTY)) {
            currentModel = model.getOverrides().apply(model, stack, MinecraftClient.getInstance().world, entity, light);
        }
        MinecraftClient.getInstance().world.getProfiler().push("QuadPushing");
        VertexConsumer consumer = modelHolder.colorProvider.getConsumer(vertexConsumers, stack, instance, transformationMode);
        assert currentModel != null;
        for (Direction direction : Direction.values()) {
            currentModel.getQuads(null, direction, random)
                    .forEach(bakedQuad -> consumer.quad(matrices.peek(), bakedQuad, colors[0], colors[1], colors[2], light, overlay));
        }

        if (modelHolder.entityRendering()) {
            ModelTransformer.getInverse(currentModel, random).forEach(bakedQuad -> {
                consumer.quad(matrices.peek(), bakedQuad, colors[0], colors[1], colors[2], light, overlay);
            });
        }

        if (stack.getItem() instanceof ArmorItem armorItem && !modelHolder.trimMode().equals(TrimRenderer.TrimMode.NONE)) {
            ModelTransformer.getRescale(currentModel, random).forEach(bakedQuad -> {
                TrimRenderer.renderTrims(matrices, bakedQuad, modelHolder.trimMode(), light, vertexConsumers, armorItem.getMaterial(), stack);
            });
        }

        MinecraftClient.getInstance().world.getProfiler().pop();
        MinecraftClient.getInstance().world.getProfiler().pop();
        matrices.pop();
    }

    private void renderTrim(ArmorMaterial material, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ArmorTrim trim, BakedModel model, boolean leggings) {
        Sprite sprite = this.armorTrimsAtlas.getSprite(leggings ? trim.getLeggingsModelId(material) : trim.getGenericModelId(material));
        VertexConsumer vertexConsumer = sprite.getTextureSpecificVertexConsumer(vertexConsumers.getBuffer(TexturedRenderLayers.getArmorTrims()));

        for (Direction direction : Direction.values()) {
            model.getQuads(null, direction, random)
                    .forEach(bakedQuad -> vertexConsumer.quad(matrices.peek(), bakedQuad, colors[0], colors[1], colors[2], light, OverlayTexture.DEFAULT_UV));
        }
    }

    public record ModelHolder(BakedModel model, Matrix4f matrix4f, ColorProvider colorProvider,
                              TrimRenderer.TrimMode trimMode, boolean entityRendering) {

    }
}
