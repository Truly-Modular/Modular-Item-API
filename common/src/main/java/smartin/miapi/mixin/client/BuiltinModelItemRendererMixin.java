package smartin.miapi.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.client.model.CustomModel;
import smartin.miapi.client.modelrework.MiapiItemModel;
import smartin.miapi.item.modular.ModularItem;

@Mixin(BuiltinModelItemRenderer.class)
public class BuiltinModelItemRendererMixin {
    @Inject(
            method = "render(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
            at = @At("HEAD")
    )
    private void miapi$customItemRendering(
            ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci
    ) {
        if (!MiapiClient.irisLoaded && stack.getItem() instanceof ModularItem) {
            MiapiItemModel miapiModel = MiapiItemModel.getItemModel(stack);
            if (miapiModel != null) {
                miapiModel.render(matrices, stack, mode, MinecraftClient.getInstance().getTickDelta(), vertexConsumers, CustomModel.currentEntity, light, overlay);
            }
            CustomModel.currentEntity = null;
        }
    }
}
