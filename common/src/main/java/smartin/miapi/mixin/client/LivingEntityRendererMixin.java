package smartin.miapi.mixin.client;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.client.ModularElytraFeatureRenderer;

@Mixin(value = LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Inject(
            method = "<init>(Lnet/minecraft/client/render/entity/EntityRendererFactory$Context;Lnet/minecraft/client/render/entity/model/EntityModel;F)V",
            at = @At("TAIL")
    )
    private void miapi$addFeatureRenderer(EntityRendererFactory.Context ctx, EntityModel model, float shadowRadius, CallbackInfo ci) {
        LivingEntityRenderer renderer = (LivingEntityRenderer) (Object) this;
        ((LivingEntityRendererAccessor) renderer).callAddFeature(new ModularElytraFeatureRenderer(renderer, ctx.getModelLoader()));
        PlayerEntity player;
    }
}
