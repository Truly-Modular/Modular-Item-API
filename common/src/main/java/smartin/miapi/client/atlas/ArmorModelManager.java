package smartin.miapi.client.atlas;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.mixin.client.ElytraEntityModelAccessor;
import smartin.miapi.mixin.client.ElytraFeatureRendererAccessor;
import smartin.miapi.mixin.client.LivingEntityRendererAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class ArmorModelManager {
    public static List<ArmorPartProvider> partProviders = new ArrayList<>();

    static {
        partProviders.add(new ModelPartProvider());
        partProviders.add(new ElytraPartProvider());
    }

    public static final class ModelPartProvider implements ArmorPartProvider {

        private static final String[] modelParts = {"head", "hat", "left_arm", "right_arm", "left_leg", "right_leg", "body"};

        @Override
        public List<ArmorPart> getParts(EquipmentSlot equipmentSlot, LivingEntity livingEntity, HumanoidModel<?> model, EntityModel entityModel) {
            List<ArmorPart> parts = new ArrayList<>();
            for (String key : modelParts) {
                parts.add((matrixStack, equipmentSlot1, livingEntity1, model1, entityModel1) -> {
                    entityModel1.copyPropertiesTo(model1);
                    ModelPart part = getModelPart(model1, key);
                    part.translateAndRotate(matrixStack);
                    return key;
                });
            }
            return parts;
        }

        private static ModelPart getModelPart(HumanoidModel<?> model, String name) {
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

    public static final class ElytraPartProvider implements ArmorPartProvider {
        @Override
        public List<ArmorPart> getParts(EquipmentSlot equipmentSlot, LivingEntity livingEntity, HumanoidModel<?> model, EntityModel entityModel) {
            List<ArmorPart> parts = new ArrayList<>();
            if (Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(livingEntity) instanceof LivingEntityRenderer livingEntityRenderer) {
                Optional<ElytraLayer<?, ?>> elytraFeatureRenderer =
                ((LivingEntityRendererAccessor) livingEntityRenderer).getFeatures().stream().filter(a -> a instanceof ElytraLayer<?, ?>).findAny();
                if(elytraFeatureRenderer.isPresent()){
                    ElytraModel elytraEntityModel = ((ElytraFeatureRendererAccessor)elytraFeatureRenderer.get()).getElytra();
                    parts.add((matrixStack, equipmentSlot1, livingEntity1, model1, entityModel1) -> {
                        entityModel.copyPropertiesTo(elytraEntityModel);
                        entityModel1.copyPropertiesTo(model1);
                        ModelPart part = ((ElytraEntityModelAccessor) elytraEntityModel).getLeftWing();
                        part.translateAndRotate(matrixStack);
                        return "left_wing";
                    });
                    parts.add((matrixStack, equipmentSlot1, livingEntity1, model1, entityModel1) -> {
                        entityModel.copyPropertiesTo(elytraEntityModel);
                        entityModel1.copyPropertiesTo(model1);
                        ModelPart part = ((ElytraEntityModelAccessor) elytraEntityModel).getRightWing();
                        part.translateAndRotate(matrixStack);
                        return "right_wing";
                    });
                    return parts;

                }
            }
            return parts;
        }
    }

    public static void renderArmorPiece(PoseStack matrices, MultiBufferSource vertexConsumers, int light, EquipmentSlot armorSlot, ItemStack itemStack, LivingEntity entity, HumanoidModel outerModel, EntityModel entityModel) {
        partProviders.forEach(armorPartProvider -> {
            List<ArmorPart> armorParts = armorPartProvider.getParts(armorSlot, entity, outerModel, entityModel);
            armorParts.forEach(armorPart -> {
                matrices.pushPose();
                String key = armorPart.apply(matrices, armorSlot, entity, outerModel, entityModel);
                MiapiItemModel miapiItemModel = MiapiItemModel.getItemModel(itemStack);
                if (miapiItemModel != null) {
                    miapiItemModel.render(key, matrices, ItemDisplayContext.HEAD, 0, vertexConsumers, light, OverlayTexture.NO_OVERLAY);
                }
                matrices.popPose();
            });
        });
    }

    public interface ArmorPartProvider {
        List<ArmorPart> getParts(EquipmentSlot equipmentSlot, LivingEntity livingEntity, HumanoidModel<?> model, EntityModel entityModel);
    }

    public interface ArmorPart {
        String apply(PoseStack matrixStack, EquipmentSlot equipmentSlot, LivingEntity livingEntity, HumanoidModel<?> model, EntityModel entityModel);
    }
}
