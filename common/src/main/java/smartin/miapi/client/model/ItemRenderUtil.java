package smartin.miapi.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import smartin.miapi.client.modelrework.MiapiItemModel;
import smartin.miapi.mixin.client.ItemRendererAccessor;

import java.util.List;

import static net.minecraft.client.render.item.ItemRenderer.getDirectItemGlintConsumer;

@Environment(EnvType.CLIENT)
public class ItemRenderUtil {
    private ItemRenderUtil() {

    }

    public static void renderModel(MatrixStack matrices, ItemStack stack, BakedModel model, ModelTransformationMode renderMode, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        RenderLayer renderLayer = RenderLayers.getItemLayer(stack, true);
        renderModel(matrices, stack, model, renderMode, vertexConsumers, renderLayer, light, overlay);
    }

    public static void renderModel(MatrixStack matrices, ItemStack stack, BakedModel model, ModelTransformationMode renderMode, VertexConsumerProvider vertexConsumers, RenderLayer renderLayer, int light, int overlay) {
        VertexConsumer vertexConsumer = getDirectItemGlintConsumer(vertexConsumers, renderLayer, true, stack.hasGlint());
        model.getTransformation().getTransformation(renderMode).apply(true, matrices);
        MiapiItemModel.getItemModel(stack).render(matrices, renderMode, 0, vertexConsumers, light, overlay);
    }

    private static void renderBakedItemQuads(MatrixStack matrices, VertexConsumer vertices, List<BakedQuad> quads, ItemStack stack, int light, int overlay) {
        boolean bl = !stack.isEmpty();
        MatrixStack.Entry entry = matrices.peek();

        for (BakedQuad bakedQuad : quads) {
            int i = -1;
            if (bl && bakedQuad.hasColor()) {
                i = ((ItemRendererAccessor) MinecraftClient.getInstance().getItemRenderer()).color().getColor(stack, bakedQuad.getColorIndex());
            }

            float f = (i >> 16 & 255) / 255.0F;
            float g = (i >> 8 & 255) / 255.0F;
            float h = (i & 255) / 255.0F;
            vertices.quad(entry, bakedQuad, f, g, h, light, overlay);
        }

    }
}
