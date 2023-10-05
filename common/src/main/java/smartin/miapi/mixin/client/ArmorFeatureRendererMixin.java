package smartin.miapi.mixin.client;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.client.ArmorModelManager;
import smartin.miapi.item.modular.ModularItem;

@Mixin(value = ArmorFeatureRenderer.class, priority = 700)
public abstract class ArmorFeatureRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> extends FeatureRenderer<T, M> {

    protected ArmorFeatureRendererMixin(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    void miapi$renderArmorInject(MatrixStack matrices, VertexConsumerProvider vertexConsumers, T entity, EquipmentSlot armorSlot, int light, A model, CallbackInfo ci) {
        ItemStack itemStack = entity.getEquippedStack(armorSlot);
        ArmorFeatureRenderer renderer = (ArmorFeatureRenderer) (Object) this;

        if (itemStack.getItem() instanceof ModularItem) {
            renderPieces(matrices, vertexConsumers, light, armorSlot, itemStack, entity, model, ((FeatureRendererAccessor) renderer).getContext());
            ci.cancel();
        }
    }

    @Unique
    private void renderPieces(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, EquipmentSlot armorSlot, ItemStack itemStack, T entity, A outerModel, FeatureRendererContext context) {
        this.getContextModel().copyBipedStateTo(outerModel);
        ArmorModelManager.renderArmorPiece(matrices, vertexConsumers, light, armorSlot, itemStack, entity, outerModel, this.getContextModel(),context);
    }
}
