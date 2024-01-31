package smartin.miapi.client.modelrework;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.render.colorproviders.ColorProvider;

public class BadShaderCompatModel implements MiapiModel {
    ModuleInstance instance;
    Material material;
    BakedModel model;
    Matrix4f modelMatrix;
    Color color;
    Random random = Random.create();
    BakedMiapiModel.ModelHolder modelHolder;
    float randomScaleNoZ = (float) (1 + Math.random() / 10000f);

    public BadShaderCompatModel(BakedMiapiModel.ModelHolder holder, ModuleInstance instance, ItemStack stack) {
        this.instance = instance;
        material = MaterialProperty.getMaterial(instance);
        if (material != null && holder.colorProvider() instanceof ColorProvider.MaterialColorProvider) {
            color = new Color(material.getColor());
        } else {
            color = holder.colorProvider().getVertexColor();
        }
        modelMatrix = holder.matrix4f();
        model = holder.model();
        this.modelHolder = holder;
    }

    @Override
    public void render(MatrixStack matrices, ItemStack stack, ModelTransformationMode transformationMode, float tickDelta, VertexConsumerProvider vertexConsumers, LivingEntity entity, int light, int overlay) {
        MinecraftClient.getInstance().world.getProfiler().push("BakedModel");
        matrices.push();
        Matrix4f matrix4f = new Matrix4f();
        matrix4f = matrix4f.scale(new Vector3f(randomScaleNoZ, randomScaleNoZ, randomScaleNoZ));
        matrices.multiplyPositionMatrix(matrix4f);
        matrices.multiplyPositionMatrix(modelMatrix);
        BakedModel currentModel = model;
        if (model.getOverrides() != null && !model.getOverrides().equals(ModelOverrideList.EMPTY)) {
            currentModel = model.getOverrides().apply(model, stack, MinecraftClient.getInstance().world, entity, light);
        }
        RenderLayer renderLayer = RenderLayers.getItemLayer(stack, true);
        VertexConsumer consumer = ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, renderLayer, true, stack.hasGlint());
        for (Direction direction : Direction.values()) {
            currentModel.getQuads(null, direction, random).forEach(bakedQuad -> {
                consumer.quad(matrices.peek(), bakedQuad, color.redAsFloat(), color.greenAsFloat(), color.blueAsFloat(), light, overlay);
            });
        }
        if (modelHolder.entityRendering()) {
            ModelTransformer.getInverse(currentModel, random).forEach(bakedQuad -> {
                consumer.quad(matrices.peek(), bakedQuad, color.redAsFloat(), color.greenAsFloat(), color.blueAsFloat(), light, overlay);
            });
        }

        if (stack.getItem() instanceof ArmorItem armorItem && !modelHolder.trimMode().equals(TrimRenderer.TrimMode.NONE)) {
            ModelTransformer.getRescale(currentModel, random).forEach(bakedQuad -> {
                TrimRenderer.renderTrims(matrices, bakedQuad, modelHolder.trimMode(), light, vertexConsumers, armorItem.getMaterial(), stack);
            });
        }
        MinecraftClient.getInstance().world.getProfiler().pop();
        matrices.pop();
    }

    public Matrix4f subModuleMatrix() {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f = matrix4f.scale(new Vector3f(1.001f, 1.001f, 1.001f));
        return matrix4f;
    }
}
