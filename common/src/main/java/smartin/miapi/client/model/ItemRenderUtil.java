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
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import smartin.miapi.mixin.ItemRendererAccessor;

import java.util.Iterator;
import java.util.List;

import static net.minecraft.client.render.item.ItemRenderer.getDirectItemGlintConsumer;

@Environment(EnvType.CLIENT)
public class ItemRenderUtil {
    private ItemRenderUtil(){

    }

    public static void renderModel(MatrixStack matrices, ItemStack stack, BakedModel model, ModelTransformation.Mode renderMode, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        RenderLayer renderLayer = RenderLayers.getItemLayer(stack, true);
        renderModel(matrices, stack, model, renderMode, vertexConsumers, renderLayer, light, overlay);
    }

    public static void renderModel(MatrixStack matrices, ItemStack stack, BakedModel model, ModelTransformation.Mode renderMode, VertexConsumerProvider vertexConsumers, RenderLayer renderLayer, int light, int overlay) {
        VertexConsumer vertexConsumer = getDirectItemGlintConsumer(vertexConsumers, renderLayer, true, stack.hasGlint());
        model.getTransformation().getTransformation(renderMode).apply(true, matrices);
        matrices.translate(-0.5, -0.5, -0.5);
        renderBakedItemQuads(matrices, vertexConsumer, model.getQuads(null, null, null), stack, light, overlay);
    }

    private static void renderBakedItemQuads(MatrixStack matrices, VertexConsumer vertices, List<BakedQuad> quads, ItemStack stack, int light, int overlay) {
        boolean bl = !stack.isEmpty();
        MatrixStack.Entry entry = matrices.peek();
        Iterator var9 = quads.iterator();

        while (var9.hasNext()) {
            BakedQuad bakedQuad = (BakedQuad) var9.next();
            int i = -1;
            if (bl && bakedQuad.hasColor()) {
                i = ((ItemRendererAccessor) MinecraftClient.getInstance().getItemRenderer()).color().getColor(stack, bakedQuad.getColorIndex());
            }

            float f = (float) (i >> 16 & 255) / 255.0F;
            float g = (float) (i >> 8 & 255) / 255.0F;
            float h = (float) (i & 255) / 255.0F;
            vertices.quad(entry, bakedQuad, f, g, h, light, overlay);
        }

    }
}
