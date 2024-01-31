package smartin.miapi.client.modelrework;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
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
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.GlintProperty;
import smartin.miapi.registries.RegistryInventory;

public class BakedMiapiGlintModel implements MiapiModel {
    ModuleInstance instance;
    Material material;
    BakedModel model;
    Matrix4f modelMatrix;
    Color color;
    BakedMiapiModel.ModelHolder modelHolder;
    GlintProperty.GlintSettings settings;
    GlintProperty.GlintSettings rootSettings;
    Random random = Random.create();

    public BakedMiapiGlintModel(BakedMiapiModel.ModelHolder holder, ModuleInstance instance, ItemStack stack) {
        modelHolder = holder;
        this.instance = instance;
        material = MaterialProperty.getMaterial(instance);
        color = holder.colorProvider().getVertexColor();
        modelMatrix = holder.matrix4f();
        model = holder.model();
        settings = GlintProperty.property.getGlintSettings(instance, stack);
        rootSettings = GlintProperty.property.getGlintSettings(instance.getRoot(), stack);
    }

    @Override
    public void render(MatrixStack matrices, ItemStack stack, ModelTransformationMode transformationMode, float tickDelta, VertexConsumerProvider vertexConsumerProvider, LivingEntity entity, int light, int overlay) {
        MinecraftClient.getInstance().world.getProfiler().push("BakedGlintModel");
        matrices.push();
        matrices.multiplyPositionMatrix(modelMatrix);
        BakedModel currentModel = model;
        if (model.getOverrides() != null && !model.getOverrides().equals(ModelOverrideList.EMPTY)) {
            currentModel = model.getOverrides().apply(model, stack, MinecraftClient.getInstance().world, entity, light);
        }
        rootSettings.applySpeed();
        settings.applyAlpha();
        Color glintColor = settings.getColor();
        VertexConsumer materialConsumer = modelHolder.colorProvider().getConsumer(vertexConsumerProvider, stack, instance, transformationMode);
        for (Direction direction : Direction.values()) {
            currentModel.getQuads(null, direction, random)
                    .forEach(bakedQuad ->
                            materialConsumer.quad(
                                    matrices.peek(),
                                    bakedQuad,
                                    color.redAsFloat(),
                                    color.greenAsFloat(),
                                    color.blueAsFloat(),
                                    light,
                                    overlay));
        }
        if (modelHolder.entityRendering()) {
            ModelTransformer.getInverse(currentModel, random).forEach(bakedQuad -> {
                materialConsumer.quad(matrices.peek(), bakedQuad, color.redAsFloat(),
                        color.greenAsFloat(),
                        color.blueAsFloat(), light, overlay);
            });
        }

        if (stack.getItem() instanceof ArmorItem armorItem && !modelHolder.trimMode().equals(TrimRenderer.TrimMode.NONE)) {
            ModelTransformer.getRescale(currentModel, random).forEach(bakedQuad -> {
                TrimRenderer.renderTrims(matrices, bakedQuad, modelHolder.trimMode(), light, vertexConsumerProvider, armorItem.getMaterial(), stack);
            });
        }
        VertexConsumer glintConsumer = vertexConsumerProvider.getBuffer(RegistryInventory.Client.modularItemGlint);
        for (Direction direction : Direction.values()) {
            currentModel.getQuads(null, direction, random)
                    .forEach(bakedQuad ->
                            glintConsumer.quad(
                                    matrices.peek(),
                                    bakedQuad,
                                    glintColor.redAsFloat(),
                                    glintColor.greenAsFloat(),
                                    glintColor.blueAsFloat(),
                                    light,
                                    overlay));
        }
        MinecraftClient.getInstance().world.getProfiler().pop();
        matrices.pop();
    }
}
