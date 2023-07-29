package smartin.miapi.client.modelrework;

import net.minecraft.client.render.LightmapTextureManager;
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
        if (!(vertexConsumers instanceof VertexConsumerProvider.Immediate immediate)) return;

        int j = 0;
        for (BakedModel model : models) {
            if (j < models.size()-1) {
                j++;
                continue;
            }
            for (Direction direction : Direction.values()) {
                for (int i = 0; i < 2; i++) {
                    if (i == 1 && !stack.hasGlint()) continue;
                    VertexConsumer consumer;
                    float r, g, b;
                    if (i == 0) {
                        r = 1;
                        g = 1;
                        b = 1;
                        if (material != null)
                            consumer = material.setupMaterialShader(immediate, RegistryInventory.Client.entityTranslucentMaterialRenderType, RegistryInventory.Client.entityTranslucentMaterialShader);
                        else
                            consumer = MaterialProperty.Material.setupMaterialShader(immediate, RegistryInventory.Client.entityTranslucentMaterialRenderType, RegistryInventory.Client.entityTranslucentMaterialShader, MaterialProperty.Material.baseColorPalette);
                    } else {
                        consumer = immediate.getBuffer(RegistryInventory.Client.modularItemGlint);
                        r = 1f;
                        g = 1;
                        b = 1f;
                    }

                    int lightValue = transformationMode == ModelTransformationMode.GUI ? LightmapTextureManager.MAX_LIGHT_COORDINATE : LightmapTextureManager.MAX_SKY_LIGHT_COORDINATE;
                    model.getQuads(null, direction, Random.create()).forEach(bakedQuad -> {
                        consumer.quad(matrices.peek(), bakedQuad, r, g, b, lightValue, overlay);
                    });
                    immediate.draw();
                }
            }
            j++;
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
