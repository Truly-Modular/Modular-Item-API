package smartin.miapi.mixin.client;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.client.model.CustomModel;

@Debug(export = true)
@Mixin(value = ItemRenderer.class, priority = 750)
public class ItemRendererMixin {

    @Inject(
            method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V",
                    shift = At.Shift.AFTER
            )
    )
    private void miapi$customItemRendering(
            ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded,
            MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
            int overlay, BakedModel model, CallbackInfo ci
    ) {
        /*
        if (stack.getItem() instanceof ModularItem) {
            MiapiItemModel miapiModel = MiapiItemModel.getItemModel(stack);
            if (miapiModel != null) {
                miapiModel.render(matrices, stack, renderMode, MinecraftClient.getInstance().getTickDelta(), vertexConsumers, entity, light, overlay);
            }
            entity = null;
        }
         */
    }

    @Inject(
            method = "Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V",
            at = @At("HEAD")
    )
    private void miapi$customItemRenderingEntityGetter(LivingEntity entity, ItemStack item, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, World world, int light, int overlay, int seed, CallbackInfo ci) {
        CustomModel.currentEntity = entity;
    }
}
