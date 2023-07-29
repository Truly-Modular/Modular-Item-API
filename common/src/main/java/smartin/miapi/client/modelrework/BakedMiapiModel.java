package smartin.miapi.client.modelrework;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.MaterialProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.List;

public class BakedMiapiModel implements MiapiModel {
    ItemModule.ModuleInstance instance;
    MaterialProperty.Material material;
    List<BakedModel> models;

    public BakedMiapiModel(List<BakedModel> models, ItemModule.ModuleInstance instance) {
        this.models = models;
        this.instance = instance;
        material = MaterialProperty.getMaterial(instance);
    }

    @Override
    public void render(MatrixStack matrices, ItemStack stack, ModelTransformationMode transformationMode, float tickDelta, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        for (BakedModel model : models) {
            for (Direction direction : Direction.values()) {
                VertexConsumer consumer;
                if (material != null)
                    consumer = material.setupMaterialShader(immediate, RegistryInventory.Client.entityTranslucentMaterialRenderType, RegistryInventory.Client.entityTranslucentMaterialShader);
                else
                    consumer = MaterialProperty.Material.setupMaterialShader(immediate, RegistryInventory.Client.entityTranslucentMaterialRenderType, RegistryInventory.Client.entityTranslucentMaterialShader, MaterialProperty.Material.baseColorPalette);
                model.getQuads(null, direction, Random.create()).forEach(bakedQuad -> {
                    consumer.quad(matrices.peek(), bakedQuad, 1, 1, 1, light, overlay);
                });
                immediate.draw();
            }
        }
    }

    @Override
    public @Nullable Matrix4f subModuleMatrix(int submoduleId) {
        Matrix4f matrix4f = new Matrix4f();
        float zPrevention = 8e-2F;
        //matrix4f.translate(new Vector3f(-zPrevention/2, -zPrevention/2, -zPrevention/2));
        //matrix4f.scale(zPrevention + 1);
        return matrix4f;
    }
}
