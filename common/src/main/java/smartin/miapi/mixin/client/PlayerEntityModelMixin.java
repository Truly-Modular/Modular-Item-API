package smartin.miapi.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.item.modular.items.armor.ModularElytraItem;

import java.util.Optional;

@Mixin(ElytraLayer.class)
public class PlayerEntityModelMixin<T extends LivingEntity> {

    @Inject(
            method = "Lnet/minecraft/client/renderer/entity/layers/ElytraLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At(value = "HEAD")
    )
    public void adjustElytraAngles(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(livingEntity) instanceof PlayerRenderer livingEntityRenderer) {
            Optional<ElytraLayer> elytraFeatureRenderer =
                    ((LivingEntityRendererAccessor) livingEntityRenderer).getFeatures().stream().filter(a -> a instanceof ElytraLayer<?, ?>).findAny();
            if (elytraFeatureRenderer.isPresent()) {
                ItemStack itemStack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
                if (!itemStack.is(Items.ELYTRA) && itemStack.getItem() instanceof ModularElytraItem) {

                    ((ElytraFeatureRendererAccessor) elytraFeatureRenderer.get()).getElytra().setupAnim(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                }
            }
        }
    }
}
