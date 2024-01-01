package smartin.miapi.client.modelrework;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;

import java.util.List;

public class AltBakedMiapiModel implements MiapiModel {
    ItemModule.ModuleInstance instance;
    Material material;
    BakedModel model;
    Matrix4f modelMatrix;
    Color color;
    BakedMiapiModel.ModelHolder modelHolder;
    Random random = Random.create();
    float[] colors;
    Sprite textureSprite = null;

    public AltBakedMiapiModel(BakedMiapiModel.ModelHolder holder, ItemModule.ModuleInstance instance, ItemStack stack) {
        modelHolder = holder;
        this.instance = instance;
        material = MaterialProperty.getMaterial(instance);
        color = holder.colorProvider().getVertexColor();
        modelMatrix = holder.matrix4f();
        model = holder.model();
        colors = new float[]{color.redAsFloat(), color.greenAsFloat(), color.blueAsFloat()};
        List<BakedQuad> quadsList = holder.model().getQuads(null, Direction.DOWN, random);
        if (quadsList.size() > 0) {
            textureSprite = holder.model().getQuads(null, Direction.DOWN, random).get(0).getSprite();
            SpriteContents contents = textureSprite.getContents();
            textureSprite = new Sprite(new Identifier(Miapi.MOD_ID, "module_model_texture_99"), contents, contents.getWidth(), contents.getHeight(), contents.getWidth(), contents.getHeight());

        }
    }

    @Override
    public void render(MatrixStack matrices, ItemStack stack, ModelTransformationMode transformationMode, float tickDelta, VertexConsumerProvider vertexConsumers, LivingEntity entity, int light, int overlay) {
        assert MinecraftClient.getInstance().world != null;
        if (textureSprite == null) return;
        MinecraftClient.getInstance().world.getProfiler().push("BakedModel");
        matrices.push();
        matrices.multiplyPositionMatrix(modelMatrix);
        BakedModel currentModel = model;
        if (model.getOverrides() != null && !model.getOverrides().equals(ModelOverrideList.EMPTY)) {
            currentModel = model.getOverrides().apply(model, stack, MinecraftClient.getInstance().world, entity, light);
        }
        MinecraftClient.getInstance().world.getProfiler().push("QuadPushing");
        //VertexConsumer consumer = modelHolder.colorProvider.getConsumer(vertexConsumers, stack, instance, transformationMode);
        RenderLayer renderLayer = RenderLayers.getItemLayer(stack, true);
        VertexConsumer consumerOld = ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, renderLayer, true, stack.hasGlint());
        VertexConsumer consumer = textureSprite.getTextureSpecificVertexConsumer(consumerOld);
        assert currentModel != null;
        for (Direction direction : Direction.values()) {
            currentModel.getQuads(null, direction, random)
                    .forEach(bakedQuad -> consumer.quad(matrices.peek(), bakedQuad, colors[0], colors[1], colors[2], light, overlay));
        }

        MinecraftClient.getInstance().world.getProfiler().pop();
        MinecraftClient.getInstance().world.getProfiler().pop();
        matrices.pop();
    }
}
