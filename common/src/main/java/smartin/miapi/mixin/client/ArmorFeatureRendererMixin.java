package smartin.miapi.mixin.client;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.client.modelrework.MiapiItemModel;
import smartin.miapi.item.modular.ModularItem;

import java.util.Arrays;

@Mixin(value = ArmorFeatureRenderer.class, priority = 700)
public abstract class ArmorFeatureRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> extends FeatureRenderer<T, M> {
    @Shadow
    @Final
    private A outerModel;

    protected ArmorFeatureRendererMixin(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Shadow
    protected abstract A getModel(EquipmentSlot slot);

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    void miapi$renderArmorInject(MatrixStack matrices, VertexConsumerProvider vertexConsumers, T entity, EquipmentSlot armorSlot, int light, A model, CallbackInfo ci) {
        ItemStack itemStack = entity.getEquippedStack(armorSlot);
        if (itemStack.getItem() instanceof ModularItem) {
            // Invert the light direction doesnt work
            int invertedLight = light;
            renderPieces(matrices, vertexConsumers, invertedLight, armorSlot, itemStack, entity);
            ci.cancel();
        }
    }

    @Unique
    private void renderPieces(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, EquipmentSlot armorSlot, ItemStack itemStack, T entity) {
        Arrays.stream(modelParts).forEach(partId -> {
            A armorModel = getModel(armorSlot);
            //TODO:make sure this works
            //(this.getContextModel()).setAttributes(armorModel);
            if (true) {
                MiapiItemModel model1 = MiapiItemModel.getItemModel(itemStack);
                if (model1 != null) {
                    MatrixStack matrixStack = new MatrixStack();
                    matrixStack.multiplyPositionMatrix(matrices.peek().getPositionMatrix());
                    matrixStack.push();
                    getModelPart(armorModel, partId).rotate(matrixStack);
                    model1.render(partId, matrixStack, ModelTransformationMode.HEAD, 0, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
                    matrixStack.pop();
                }
            }
        });
    }

    private static final String[] modelParts = {"head", "hat", "left_arm", "right_arm", "left_leg", "right_leg", "body"};

    private static ModelPart getModelPart(BipedEntityModel<?> model, String name) {
        return switch (name) {
            case "head" -> model.head;
            case "hat" -> model.hat;
            case "left_arm" -> model.leftArm;
            case "right_arm" -> model.rightArm;
            case "left_leg" -> model.leftLeg;
            case "right_leg" -> model.rightLeg;
            default -> model.body;
        };
    }
}
