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
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.GlintProperty;
import smartin.miapi.registries.RegistryInventory;

public class BakedMiapiGlintModel implements MiapiModel {
    ItemModule.ModuleInstance instance;
    Material material;
    BakedModel model;
    Matrix4f modelMatrix;
    Color color;
    BakedMiapiModel.ModelHolder modelHolder;
    GlintProperty.GlintSettings settings;
    GlintProperty.GlintSettings rootSettings;

    public BakedMiapiGlintModel(BakedMiapiModel.ModelHolder holder, ItemModule.ModuleInstance instance, ItemStack stack) {
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
    public void render(MatrixStack matrices, ItemStack stack, ModelTransformationMode transformationMode, float tickDelta, VertexConsumerProvider vertexConsumers, LivingEntity entity, int light, int overlay) {
        if (!(vertexConsumers instanceof VertexConsumerProvider.Immediate)) return;
        MinecraftClient.getInstance().world.getProfiler().push("BakedGlintModel");
        matrices.push();
        matrices.multiplyPositionMatrix(modelMatrix);
        for (Direction direction : Direction.values()) {
            if (model.getOverrides() != null && !model.getOverrides().equals(ModelOverrideList.EMPTY)) {
                model = model.getOverrides().apply(model, stack, MinecraftClient.getInstance().world, entity, light);
            }
            VertexConsumer consumer = modelHolder.colorProvider().getConsumer(vertexConsumers, stack, instance, transformationMode);

            model.getQuads(null, direction, Random.create()).forEach(bakedQuad -> {
                consumer.quad(matrices.peek(), bakedQuad, color.redAsFloat(), color.greenAsFloat(), color.blueAsFloat(), light, overlay);
            });

            rootSettings.applySpeed();
            settings.applyAlpha();
            VertexConsumer glintConsumer = vertexConsumers.getBuffer(RegistryInventory.Client.modularItemGlint);

            Color glintColor = settings.getColor();
            model.getQuads(null, direction, Random.create()).forEach(bakedQuad -> {
                glintConsumer.quad(matrices.peek(), bakedQuad, (float) glintColor.r() / 255, (float) glintColor.g() / 255, (float) glintColor.b() / 255, light, overlay);
            });
        }
        MinecraftClient.getInstance().world.getProfiler().pop();
        matrices.pop();
    }

    @Override
    public @Nullable Matrix4f subModuleMatrix() {
        return new Matrix4f();
    }
}
