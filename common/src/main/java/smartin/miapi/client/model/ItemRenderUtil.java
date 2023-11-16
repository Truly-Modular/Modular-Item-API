package smartin.miapi.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import smartin.miapi.client.modelrework.MiapiItemModel;

@Environment(EnvType.CLIENT)
public class ItemRenderUtil {
    private ItemRenderUtil() {

    }

    public static void renderModel(MatrixStack matrices, ItemStack stack, BakedModel model, ModelTransformationMode renderMode, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        RenderLayer renderLayer = RenderLayers.getItemLayer(stack, true);
        renderModel(matrices, stack, model, renderMode, vertexConsumers, renderLayer, light, overlay);
    }

    public static void renderModel(MatrixStack matrices, ItemStack stack, BakedModel model, ModelTransformationMode renderMode, VertexConsumerProvider vertexConsumers, RenderLayer renderLayer, int light, int overlay) {
        model.getTransformation().getTransformation(renderMode).apply(true, matrices);
        MiapiItemModel itemModel = MiapiItemModel.getItemModel(stack);
        if(itemModel != null){
            MiapiItemModel.getItemModel(stack).render(matrices, renderMode, 0, vertexConsumers, light, overlay);
        }
    }
}
