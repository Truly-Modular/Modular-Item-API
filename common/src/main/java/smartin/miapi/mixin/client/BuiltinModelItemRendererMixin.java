package smartin.miapi.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.client.model.item.ItemBakedModelReplacement;
import smartin.miapi.item.modular.VisualModularItem;

@Mixin(BlockEntityWithoutLevelRenderer.class)
public class BuiltinModelItemRendererMixin {
    @Inject(
            method = "renderByItem",
            at = @At("HEAD"),
            cancellable = true
    )
    private void miapi$customItemRendering(
            ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, CallbackInfo ci
    ) {
        if (!MiapiClient.shaderModLoaded && stack.getItem() instanceof VisualModularItem) {
            MiapiItemModel miapiModel = MiapiItemModel.getItemModel(stack);
            if (miapiModel != null) {
                miapiModel.render(matrices, stack, mode, Minecraft.getInstance().getTimer().getGameTimeDeltaTicks(), vertexConsumers, ItemBakedModelReplacement.currentEntity, light, overlay);
                ItemBakedModelReplacement.currentEntity = null;
                ci.cancel();
            }
        }
    }
}
