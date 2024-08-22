package smartin.miapi.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(PlayerEntityModel.class)
public class PlayerEntityModelMixin<T extends LivingEntity> {

    @Inject(
            method = "Lnet/minecraft/client/render/entity/model/PlayerEntityModel;setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
            at = @At(value = "HEAD")
    )
    public void adjustElytraAngles(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(livingEntity) instanceof LivingEntityRenderer livingEntityRenderer) {
            Optional<ElytraFeatureRenderer<?, ?>> elytraFeatureRenderer =
                    ((LivingEntityRendererAccessor) livingEntityRenderer).getFeatures().stream().filter(a -> a instanceof ElytraFeatureRenderer<?, ?>).findAny();
            if (elytraFeatureRenderer.isPresent()) {
                ((ElytraFeatureRendererAccessor) elytraFeatureRenderer.get()).getElytra().setAngles(livingEntity, f, g, h, i, j);
            }
        }
    }
}
