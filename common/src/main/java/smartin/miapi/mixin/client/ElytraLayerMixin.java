package smartin.miapi.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.item.modular.VisualModularItem;

@Mixin(ElytraLayer.class)
public class ElytraLayerMixin {
    @Inject(
            method = "Lnet/minecraft/client/renderer/entity/layers/ElytraLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At("HEAD")
    )
    private <T extends LivingEntity, M extends EntityModel<T>> void miapi$customItemRenderingEntityGetter(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        ItemStack itemStack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
        if (VisualModularItem.isVisualModularItem(itemStack)) {
            ElytraLayer layer = (ElytraLayer) (Object) this;
            ((ElytraFeatureRendererAccessor) layer).getMiapiElytra().setupAnim(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }
}
