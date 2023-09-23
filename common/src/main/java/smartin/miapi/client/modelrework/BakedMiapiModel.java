package smartin.miapi.client.modelrework;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
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
import smartin.miapi.modules.properties.GlintProperty;
import smartin.miapi.modules.properties.material.Material;
import smartin.miapi.modules.properties.material.MaterialProperty;
import smartin.miapi.modules.properties.render.colorproviders.ColorProvider;
import smartin.miapi.registries.RegistryInventory;

import java.util.List;

public class BakedMiapiModel implements MiapiModel {
    ItemModule.ModuleInstance instance;
    Material material;
    List<ModelHolder> models;
    GlintProperty.GlintSettings settings;
    GlintProperty.GlintSettings rootSettings;

    public BakedMiapiModel(List<ModelHolder> models, ItemModule.ModuleInstance instance, ItemStack stack) {
        this.models = models;
        this.instance = instance;
        material = MaterialProperty.getMaterial(instance);
        settings = GlintProperty.property.getGlintSettings(instance, stack);
        rootSettings = GlintProperty.property.getGlintSettings(instance.getRoot(), stack);
    }

    @Override
    public void render(MatrixStack matrices, ItemStack stack, ModelTransformationMode transformationMode, float tickDelta, VertexConsumerProvider vertexConsumers, LivingEntity entity, int light, int overlay) {
        if (!(vertexConsumers instanceof VertexConsumerProvider.Immediate immediate)) return;

        for (ModelHolder modelholder : models) {
            BakedModel model = modelholder.model;
            Matrix4f modelMatrix = modelholder.matrix4f;
            matrices.push();
            matrices.multiplyPositionMatrix(modelMatrix);
            for (Direction direction : Direction.values()) {
                MinecraftClient.getInstance().world.getProfiler().push("BakedModel");
                if (model.getOverrides() != null && !model.getOverrides().equals(ModelOverrideList.EMPTY)) {
                    model = model.getOverrides().apply(model, stack, MinecraftClient.getInstance().world, entity, light);
                }
                VertexConsumer consumer = modelholder.colorProvider.getConsumer(vertexConsumers);
                Color color = modelholder.colorProvider.getVertexColor();

                int lightValue = transformationMode == ModelTransformationMode.GUI ? LightmapTextureManager.MAX_LIGHT_COORDINATE : LightmapTextureManager.MAX_SKY_LIGHT_COORDINATE;
                model.getQuads(null, direction, Random.create()).forEach(bakedQuad -> {
                    consumer.quad(matrices.peek(), bakedQuad, color.redAsFloat(), color.greenAsFloat(), color.blueAsFloat(), lightValue, overlay);
                });
                MinecraftClient.getInstance().world.getProfiler().pop();

                if (settings.shouldRender()) {
                    MinecraftClient.getInstance().world.getProfiler().push("Glint");
                    rootSettings.applySpeed();
                    settings.applyAlpha();
                    VertexConsumer glintConsumer = immediate.getBuffer(RegistryInventory.Client.modularItemGlint);

                    Color glintColor = settings.getColor();

                    model.getQuads(null, direction, Random.create()).forEach(bakedQuad -> {
                        //red, green, blue
                        glintConsumer.quad(matrices.peek(), bakedQuad, (float) glintColor.r() / 255, (float) glintColor.g() / 255, (float) glintColor.b() / 255, lightValue, overlay);
                    });
                    MinecraftClient.getInstance().world.getProfiler().pop();
                }
            }
            matrices.pop();
        }
    }

    public record ModelHolder(BakedModel model, Matrix4f matrix4f, ColorProvider colorProvider) {

    }

    @Override
    public @Nullable Matrix4f subModuleMatrix(int submoduleId) {
        return new Matrix4f();
    }
}
