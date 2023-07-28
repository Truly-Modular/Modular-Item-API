package smartin.miapi.client.modelrework;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
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
        for (BakedModel model : models) {
            for (Direction direction : Direction.values()) {
                if (material != null) {
                    VertexConsumer consumer = material.getMaterialConsumer(vertexConsumers);
                    model.getQuads(null, direction, Random.create()).forEach(bakedQuad -> {
                        consumer.quad(matrices.peek(), bakedQuad, 1, 1, 1, light, overlay);
                    });
                }
            }
        }
    }

    @Override
    public @Nullable Matrix4f subModuleMatrix(int submoduleId) {
        return null;
    }
}
