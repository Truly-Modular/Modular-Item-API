package smartin.miapi.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.client.model.item.ItemBakedModelReplacement;
import smartin.miapi.item.modular.VisualModularItem;

@Debug(export = true)
@Mixin(value = ItemRenderer.class, priority = 750)
public class ItemRendererMixin {

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V",
                    shift = At.Shift.AFTER
            )
    )
    private void miapi$customItemRendering(
            ItemStack stack, ItemDisplayContext renderMode, boolean leftHanded,
            PoseStack matrices, MultiBufferSource vertexConsumers, int light,
            int overlay, BakedModel model, CallbackInfo ci
    ) {
        if (MiapiClient.shaderModLoaded &&  stack.getItem() instanceof VisualModularItem) {
            MiapiItemModel miapiModel = MiapiItemModel.getItemModel(stack);
            if (miapiModel != null) {
                miapiModel.render(matrices, stack, renderMode, Minecraft.getInstance().getTimer().getRealtimeDeltaTicks(), vertexConsumers, ItemBakedModelReplacement.currentEntity, light, overlay);
            }
            ItemBakedModelReplacement.currentEntity = null;
        }
    }

    @Inject(
            method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V",
            at = @At("HEAD")
    )
    private void miapi$customItemRenderingEntityGetter(LivingEntity entity, ItemStack item, ItemDisplayContext renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource vertexConsumers, Level world, int light, int overlay, int seed, CallbackInfo ci) {
        ItemBakedModelReplacement.currentEntity = entity;
    }
}
