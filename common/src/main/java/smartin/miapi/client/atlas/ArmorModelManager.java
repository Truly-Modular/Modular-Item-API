package smartin.miapi.client.atlas;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.mixin.client.ElytraEntityModelAccessor;
import smartin.miapi.mixin.client.ElytraFeatureRendererAccessor;
import smartin.miapi.mixin.client.LivingEntityRendererAccessor;

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
        public List<ArmorPart> getParts(EquipmentSlot equipmentSlot, LivingEntity livingEntity) {
            List<ArmorPart> parts = new ArrayList<>();
            for (String key : modelParts) {
                parts.add((matrixStack, equipmentSlot1, livingEntity1, model1, entityModel1) -> {
                    entityModel1.copyStateTo(model1);
                    ModelPart part = getModelPart(model1, key);
                    part.rotate(matrixStack);
                    return key;
                });
            }
            return parts;
        }

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

    public static final class ElytraPartProvider implements ArmorPartProvider {
        @Override
        public List<ArmorPart> getParts(EquipmentSlot equipmentSlot, LivingEntity livingEntity, BipedEntityModel<?> model, EntityModel entityModel) {
            List<ArmorPart> parts = new ArrayList<>();
            if (MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(livingEntity) instanceof LivingEntityRenderer livingEntityRenderer) {
                Optional<ElytraFeatureRenderer<?, ?>> elytraFeatureRenderer =
                        ((LivingEntityRendererAccessor) livingEntityRenderer).getFeatures().stream().filter(a -> a instanceof ElytraFeatureRenderer<?, ?>).findAny();
                if (elytraFeatureRenderer.isPresent()) {
                    ElytraEntityModel elytraEntityModel = ((ElytraFeatureRendererAccessor) elytraFeatureRenderer.get()).getElytra();
                    model.copyStateTo(elytraEntityModel);
                    parts.add((matrixStack, equipmentSlot1, livingEntity1, model1, entityModel1) -> {
                        //entityModel.copyStateTo(elytraEntityModel);
                        //entityModel1.copyStateTo(model1);
                        ModelPart part = ((ElytraEntityModelAccessor) elytraEntityModel).getLeftWing();
                        part.rotate(matrixStack);
                        return "left_wing";
                    });
                    parts.add((matrixStack, equipmentSlot1, livingEntity1, model1, entityModel1) -> {
                        //entityModel.copyStateTo(elytraEntityModel);
                        //entityModel1.copyStateTo(model1);
                        ModelPart part = ((ElytraEntityModelAccessor) elytraEntityModel).getRightWing();
                        part.rotate(matrixStack);
                        return "right_wing";
                    });
                    return parts;

                }
            }
            return parts;
        }

        @Override
        public List<ArmorPart> getParts(EquipmentSlot equipmentSlot, LivingEntity livingEntity) {
            return List.of();
        }
    }

    public static void renderArmorPiece(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, EquipmentSlot armorSlot, ItemStack itemStack, LivingEntity entity, BipedEntityModel outerModel, EntityModel entityModel) {
        partProviders.forEach(armorPartProvider -> {
            List<ArmorPart> armorParts = armorPartProvider.getParts(armorSlot, entity, outerModel, entityModel);
            armorParts.forEach(armorPart -> {
                matrices.push();
                String key = armorPart.apply(matrices, armorSlot, entity, outerModel, entityModel);
                MiapiItemModel miapiItemModel = MiapiItemModel.getItemModel(itemStack);
                if (miapiItemModel != null) {
                    miapiItemModel.render(key, matrices, ModelTransformationMode.HEAD, 0, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
                }
                matrices.pop();
            });
        });
    }

    public interface ArmorPartProvider {
        default List<ArmorPart> getParts(EquipmentSlot equipmentSlot, LivingEntity livingEntity, BipedEntityModel<?> model, EntityModel entityModel) {
            return getParts(equipmentSlot, livingEntity);
        }

        List<ArmorPart> getParts(EquipmentSlot equipmentSlot, LivingEntity livingEntity);
    }

    public interface ArmorPart {
        String apply(MatrixStack matrixStack, EquipmentSlot equipmentSlot, LivingEntity livingEntity, BipedEntityModel<?> model, EntityModel entityModel);
    }
}
