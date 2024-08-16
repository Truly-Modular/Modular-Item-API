package smartin.miapi.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.client.atlas.ArmorModelManager;
import smartin.miapi.item.modular.VisualModularItem;

@Mixin(value = HumanoidArmorLayer.class, priority = 700)
public abstract class ArmorFeatureRendererMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {

    protected ArmorFeatureRendererMixin(RenderLayerParent<T, M> context) {
        super(context);
    }

    @Inject(method = "renderArmorPiece", at = @At("HEAD"), cancellable = true)
    void miapi$renderArmorInject(PoseStack matrices, MultiBufferSource vertexConsumers, T entity, EquipmentSlot armorSlot, int light, A model, CallbackInfo ci) {
        ItemStack itemStack = entity.getItemBySlot(armorSlot);
        HumanoidArmorLayer renderer = (HumanoidArmorLayer) (Object) this;

        if (itemStack.getItem() instanceof VisualModularItem) {
            renderPieces(matrices, vertexConsumers, light, armorSlot, itemStack, entity, model, ((FeatureRendererAccessor) renderer).getContext());
            ci.cancel();
        }
    }

    @Unique
    private void renderPieces(PoseStack matrices, MultiBufferSource vertexConsumers, int light, EquipmentSlot armorSlot, ItemStack itemStack, T entity, A outerModel, RenderLayerParent context) {
        this.getParentModel().copyPropertiesTo(outerModel);
        ArmorModelManager.renderArmorPiece(matrices, vertexConsumers, light, armorSlot, itemStack, entity, outerModel, this.getParentModel(),context);
    }
}
