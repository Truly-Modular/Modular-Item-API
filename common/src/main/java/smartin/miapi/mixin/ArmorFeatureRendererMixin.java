package smartin.miapi.mixin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.client.model.ItemRenderUtil;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.properties.render.ModelProperty;

import javax.annotation.Nullable;
import java.util.Arrays;

@Mixin(value = ArmorFeatureRenderer.class, priority = 700)
public abstract class ArmorFeatureRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> extends FeatureRenderer<T, M> {
    public ArmorFeatureRendererMixin(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Shadow
    protected abstract A getArmor(EquipmentSlot slot);

    @Unique
    float immersiveArmors$tickDelta;

    @Unique
    @Nullable
    ItemStack immersiveArmors$equippedStack;

    @Unique
    @Nullable
    T immersiveArmors$entity;

    @ModifyVariable(method = "renderArmor", at = @At("STORE"), ordinal = 0)
    ItemStack immersiveArmors$immersiveArmors$fetchItemStack(ItemStack itemStack) {
        this.immersiveArmors$equippedStack = itemStack;
        return itemStack;
    }

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    void immersiveArmors$injectRenderArmor(MatrixStack matrices, VertexConsumerProvider vertexConsumers, T entity, EquipmentSlot armorSlot, int light, A model, CallbackInfo ci) {
        ItemStack itemStack = entity.getEquippedStack(armorSlot);
        if (itemStack.getItem() instanceof ModularItem) {
            renderPieces(matrices, vertexConsumers, light, armorSlot, itemStack, entity);
            ci.cancel();
        }
    }

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V", at = @At("HEAD"))
    public void immersiveArmors$render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T entity, float f, float g, float tickDelta, float j, float k, float l, CallbackInfo ci) {
        this.immersiveArmors$tickDelta = tickDelta;
        this.immersiveArmors$entity = entity;
    }

    private void renderPieces(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, EquipmentSlot armorSlot, ItemStack itemStack, T entity) {
        Arrays.stream(modelParts).forEach(partId -> {
            A armorModel = getArmor(armorSlot);
            (this.getContextModel()).setAttributes(armorModel);
            BakedModel model = ModelProperty.getModelMap(itemStack).get(partId);
            if (model != null) {
                MatrixStack matrixStack = new MatrixStack();
                matrixStack.multiplyPositionMatrix(matrices.peek().getPositionMatrix());
                matrixStack.push();
                getModelPart(armorModel, partId).rotate(matrixStack);
                if(model.getOverrides()!=null){
                    //model = model.getOverrides().apply(model,itemStack, (ClientWorld) entity.getWorld(),entity,0);
                }
                ItemRenderUtil.renderModel(matrixStack, itemStack, model, ModelTransformation.Mode.HEAD, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
                matrixStack.pop();
            }
        });
    }

    private static final String[] modelParts = {"head", "hat", "left_arm", "right_arm", "left_leg", "right_leg", "body"};

    private static ModelPart getModelPart(BipedEntityModel model, String name) {
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
