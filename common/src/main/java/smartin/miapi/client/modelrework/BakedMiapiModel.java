package smartin.miapi.client.modelrework;

import com.redpxnda.nucleus.util.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import smartin.miapi.Miapi;
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
        for (BakedModel model : models) {
            for (Direction direction : Direction.values()) {
                if (material != null) {

                    //test code
                    /*if (material instanceof MaterialProperty.JsonMaterial jsonMaterial) {
                        VertexConsumer vc2 = material.setupMaterialShader(vertexConsumers, RegistryInventory.Client.translucentMaterialRenderType, RegistryInventory.Client.translucentMaterialShader);
                        Sprite sprite = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(jsonMaterial.materialColorPalette);
                        for (int i = 0; i < 6; i++) {
                            RenderUtil.addQuad(
                                    RenderUtil.CUBE[i], matrices, vc2,
                                    1f, 1f, 1f, 1f,
                                    -.25f, .25f, .25f,
                                    sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV(),
                                    light);
                        }
                        matrices.translate(0.5, 0.5, 0.5);
                    }*/
                    //test code end

                    VertexConsumer consumer = material.setupMaterialShader(vertexConsumers, RegistryInventory.Client.entityTranslucentMaterialRenderType, RegistryInventory.Client.entityTranslucentMaterialShader);
                    model.getQuads(null, direction, Random.create()).forEach(bakedQuad -> {
                        consumer.quad(matrices.peek(), bakedQuad, 1, 1, 1, light, overlay);
                    });
                }
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
