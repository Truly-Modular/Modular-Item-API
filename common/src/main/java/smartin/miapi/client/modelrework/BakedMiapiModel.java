package smartin.miapi.client.modelrework;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
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
import smartin.miapi.modules.properties.GlintProperty;
import smartin.miapi.modules.properties.material.Material;
import smartin.miapi.modules.properties.material.MaterialProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.List;

public class BakedMiapiModel implements MiapiModel {
    ItemModule.ModuleInstance instance;
    Material material;
    List<BakedModel> models;
    GlintProperty.GlintSettings settings;
    GlintProperty.GlintSettings rootSettings;

    public BakedMiapiModel(List<BakedModel> models, ItemModule.ModuleInstance instance, ItemStack stack) {
        this.models = models;
        this.instance = instance;
        material = MaterialProperty.getMaterial(instance);
        settings = GlintProperty.property.getGlintSettings(instance, stack);
        rootSettings = GlintProperty.property.getGlintSettings(instance.getRoot(), stack);
    }

    @Override
    public void render(MatrixStack matrices, ItemStack stack, ModelTransformationMode transformationMode, float tickDelta, VertexConsumerProvider vertexConsumers, LivingEntity entity, int light, int overlay) {
        if (!(vertexConsumers instanceof VertexConsumerProvider.Immediate immediate)) return;

        for (BakedModel model : models) {
            for (Direction direction : Direction.values()) {
                MinecraftClient.getInstance().world.getProfiler().push("BakedModel");
                if (model.getOverrides() != null && !model.getOverrides().equals(ModelOverrideList.EMPTY)) {
                    model = model.getOverrides().apply(model, stack, MinecraftClient.getInstance().world, entity, light);
                }
                VertexConsumer consumer;
                if (material != null)
                    consumer = material.setupMaterialShader(immediate, RegistryInventory.Client.entityTranslucentMaterialRenderType, RegistryInventory.Client.entityTranslucentMaterialShader);
                else
                    consumer = Material.setupMaterialShader(immediate, RegistryInventory.Client.entityTranslucentMaterialRenderType, RegistryInventory.Client.entityTranslucentMaterialShader, Material.baseColorPalette);

                int lightValue = transformationMode == ModelTransformationMode.GUI ? LightmapTextureManager.MAX_LIGHT_COORDINATE : LightmapTextureManager.MAX_SKY_LIGHT_COORDINATE;
                model.getQuads(null, direction, Random.create()).forEach(bakedQuad -> {
                    consumer.quad(matrices.peek(), bakedQuad, 1, 1, 1, lightValue, overlay);
                });
                immediate.draw();
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
                    immediate.draw();
                    MinecraftClient.getInstance().world.getProfiler().pop();
                }
            }
        }
    }

    @Override
    public @Nullable Matrix4f subModuleMatrix(int submoduleId) {
        return new Matrix4f();
    }
}
