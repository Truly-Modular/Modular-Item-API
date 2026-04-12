package smartin.miapi.forge.compat.epic_fight;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redpxnda.nucleus.client.Rendering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import smartin.miapi.Miapi;
import smartin.miapi.client.atlas.ArmorModelManager;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.mixin.client.ElytraEntityModelAccessor;
import smartin.miapi.mixin.client.ElytraFeatureRendererAccessor;
import smartin.miapi.mixin.client.LivingEntityRendererAccessor;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.patched.layer.ModelRenderLayer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class CustomModularArmorRenderer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends HumanoidModel<E>, AM extends HumanoidMesh> extends ModelRenderLayer<E, T, M, HumanoidArmorLayer<E, M, M>, AM> {
    public CustomModularArmorRenderer(AssetAccessor<AM> mesh) {
        super(mesh);
        addEFModelProvider(new String[]{"Root", "Torso", "Chest", "Shoulder_R", "Arm_R"}, "right_arm",
                new Transform(
                        new Vector3f(0f, 0f, 0f),
                        new Vector3f(-1 / 16f, 1 / 16f, 0f),
                        new Vector3f(-1f, 1f, 1f)).toMatrix());
        addEFModelProvider(new String[]{"Root", "Torso", "Chest", "Shoulder_L", "Arm_L"}, "left_arm",
                new Transform(
                        new Vector3f(0f, 0f, 0f),
                        new Vector3f(1 / 16f, 1 / 16f, 0f),
                        new Vector3f(-1f, 1f, 1f)).toMatrix());
        addEFModelProvider(new String[]{"Root", "Thigh_R"}, "right_leg", null, EquipmentSlot.FEET, true);
        addEFModelProvider(new String[]{"Root", "Thigh_L"}, "left_leg", null, EquipmentSlot.FEET, true);
        addEFModelProvider(new String[]{"Root", "Thigh_R", "Leg_R"}, "right_leg", new Transform(
                new Vector3f(0f, 0f, 0f),
                new Vector3f(0f, -0.3f, 0f),
                new Vector3f(1f, 1f, 1f)).toMatrix(), EquipmentSlot.FEET, false);
        addEFModelProvider(new String[]{"Root", "Thigh_L", "Leg_L"}, "left_leg", new Transform(
                new Vector3f(0f, 0f, 0f),
                new Vector3f(0f, -0.3f, 0f),
                new Vector3f(1f, 1f, 1f)).toMatrix(), EquipmentSlot.FEET, false);
        addEFModelProvider(new String[]{"Root", "Torso", "Chest", "Head"}, "head",
                new Transform(
                        new Vector3f(0f, 0f, 0f),
                        new Vector3f(0f, 0f, 0f),
                        new Vector3f(1f, -1f, 1f)).toMatrix());
        addEFModelProvider(new String[]{"Root", "Torso", "Chest", "Head"}, "hat",
                new Transform(
                        new Vector3f(0f, 0f, 0f),
                        new Vector3f(0f, 0f, 0f),
                        new Vector3f(1f, -1f, 1f)).toMatrix());
        addEFModelProvider(new String[]{"Root", "Torso"}, "chest",
                new Transform(
                        new Vector3f(0f, 0f, 0f),
                        new Vector3f(0f, 0.7f, 0f),
                        new Vector3f(1f, -1f, 1f)).toMatrix());
        addEFModelProvider(new String[]{"Root", "Torso"}, "body",
                new Transform(
                        new Vector3f(0f, 0f, 0f),
                        new Vector3f(0f, 0.7f, 0f),
                        new Vector3f(1f, -1f, 1f)).toMatrix());
    }

    public static final Matrix4f wingMatrix = new Transform(
            new Vector3f(0f, 0f, 0f),
            new Vector3f(0f, 0.4f, 0f),
            new Vector3f(1f, -1f, 1f)).toMatrix();

    private void addEFModelProvider(String[] joint, String miapiJoint) {
        epicFightModelProviders.add(new ModelProvider(joint, miapiJoint, null));
    }

    private void addEFModelProvider(String[] joint, String miapiJoint, Matrix4f matrix4f) {
        epicFightModelProviders.add(new ModelProvider(joint, miapiJoint, matrix4f));
    }

    private void addEFModelProvider(String[] joint, String miapiJoint, Matrix4f matrix4f, EquipmentSlot equipmentSlot, boolean inverse) {
        ModelProvider modelProvider = new ModelProvider(joint, miapiJoint, matrix4f);
        modelProvider.inverse = inverse;
        modelProvider.equipmentSlot = equipmentSlot;
        epicFightModelProviders.add(modelProvider);
    }

    List<ModelProvider> epicFightModelProviders = new ArrayList<>();


    @Override
    protected void renderLayer(T patch, E entity, @Nullable HumanoidArmorLayer<E, M, M> emmArmorFeatureRenderer, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, OpenMatrix4f[] openMatrix4fs, float v, float v1, float v2, float v3) {
        float partial = (float) Rendering.getGameAndPartialTime();
        renderSlot(entity, patch, emmArmorFeatureRenderer, poseStack, multiBufferSource, i, EquipmentSlot.HEAD, openMatrix4fs, partial);
        renderSlot(entity, patch, emmArmorFeatureRenderer, poseStack, multiBufferSource, i, EquipmentSlot.CHEST, openMatrix4fs, partial);
        renderSlot(entity, patch, emmArmorFeatureRenderer, poseStack, multiBufferSource, i, EquipmentSlot.LEGS, openMatrix4fs, partial);
        renderSlot(entity, patch, emmArmorFeatureRenderer, poseStack, multiBufferSource, i, EquipmentSlot.FEET, openMatrix4fs, partial);
    }

    private void renderSlot(E entity, T patch, @Nullable HumanoidArmorLayer<E, M, M> emmArmorFeatureRenderer, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, EquipmentSlot slot, OpenMatrix4f[] openMatrix4fs, float partial) {
        renderArmorPiece(matrixStack, vertexConsumerProvider, emmArmorFeatureRenderer, i, slot, entity.getItemBySlot(slot), entity, patch, openMatrix4fs, partial);
    }


    public void renderArmorPiece(PoseStack matrices, MultiBufferSource vertexConsumers, @Nullable HumanoidArmorLayer<E, M, M> emmArmorFeatureRenderer, int light, EquipmentSlot armorSlot, ItemStack itemStack, LivingEntity entity, T patch, OpenMatrix4f[] openMatrix4fs, float partial) {
        if (!VisualModularItem.isVisualModularItem(itemStack)) {
            return;
        }
        matrices.pushPose();
        Pose pose = patch.getClientAnimator().getPose(partial);
        patch.getArmature().getPoseAsTransformMatrix(pose, false);
        epicFightModelProviders.forEach(modelProvider -> {
            matrices.pushPose();
            if (modelProvider.apply(matrices, patch.getArmature()) &&
                (modelProvider.equipmentSlot == null ||
                 (modelProvider.equipmentSlot == armorSlot) == !modelProvider.inverse
                )) {
                String key = modelProvider.tmId;
                MiapiItemModel miapiItemModel = MiapiItemModel.getItemModel(itemStack);
                if (miapiItemModel != null) {
                    //matrices.mulPose(toJomlMatrix(pose.get(modelProvider.efId[modelProvider.efId.length - 1]).toMatrix()));
                    if (emmArmorFeatureRenderer != null) {
                        Miapi.LOGGER.info("not null");
                    }
                    Joint joint = patch.getArmature().searchJointByName(modelProvider.efId[modelProvider.efId.length - 1]);
                    matrices.mulPose(toJomlMatrix(patch.getArmature().getBoundTransformFor(patch.getClientAnimator().getPose(partial), joint)));
                    if (modelProvider.matrix4f != null) {
                        matrices.mulPose(modelProvider.matrix4f);
                    }
                    miapiItemModel.render(key, itemStack, matrices, ItemDisplayContext.HEAD, partial, vertexConsumers, entity, light, OverlayTexture.NO_OVERLAY);
                }
            }
            matrices.popPose();
        });
        matrices.popPose();
        matrices.pushPose();

        Joint joint = patch.getArmature().searchJointByName("Chest");
        matrices.mulPose(toJomlMatrix(patch.getArmature().getBoundTransformFor(patch.getClientAnimator().getPose(partial), joint)));
        matrices.mulPose(wingMatrix);

        if (Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity) instanceof LivingEntityRenderer livingEntityRenderer) {
            Optional<ElytraLayer<?, ?>> elytraFeatureRenderer =
                    ((LivingEntityRendererAccessor) livingEntityRenderer).getFeatures().stream().filter(a -> a instanceof ElytraLayer<?, ?>).findAny();
            MiapiItemModel miapiItemModel = MiapiItemModel.getItemModel(itemStack);
            if (elytraFeatureRenderer.isPresent() && miapiItemModel != null) {
                ElytraModel elytraEntityModel = ((ElytraFeatureRendererAccessor) elytraFeatureRenderer.get()).getElytra();
                livingEntityRenderer.getModel().copyPropertiesTo(elytraEntityModel);
                matrices.pushPose();
                ModelPart part = ((ElytraEntityModelAccessor) elytraEntityModel).getLeftWing();
                part.translateAndRotate(matrices);
                miapiItemModel.render("left_wing", itemStack, matrices, ItemDisplayContext.HEAD, 0, vertexConsumers, entity, light, OverlayTexture.NO_OVERLAY);
                matrices.popPose();
                matrices.pushPose();
                ModelPart rightWing = ((ElytraEntityModelAccessor) elytraEntityModel).getRightWing();
                ArmorModelManager armorModelManager;
                rightWing.translateAndRotate(matrices);
                miapiItemModel.render("right_wing", itemStack, matrices, ItemDisplayContext.HEAD, 0, vertexConsumers, entity, light, OverlayTexture.NO_OVERLAY);

                matrices.popPose();

            }
        }
        matrices.popPose();
    }

    public class ModelProvider {
        public String tmId;
        public String[] efId = new String[]{};
        public Matrix4f matrix4f;
        public EquipmentSlot equipmentSlot = null;
        public boolean inverse = false;

        public ModelProvider(String[] jointID, String tmId, Matrix4f matrix4f) {
            this.efId = jointID;
            this.tmId = tmId;
            this.matrix4f = matrix4f;
        }

        public boolean apply(PoseStack matrixStack, Armature armature) {
            return true;
        }
    }

    public static Matrix4f toJomlMatrix(OpenMatrix4f openMatrix4f) {
        Matrix4f jomlMatrix = new Matrix4f();

        jomlMatrix.m00(openMatrix4f.m00);
        jomlMatrix.m01(openMatrix4f.m01);
        jomlMatrix.m02(openMatrix4f.m02);
        jomlMatrix.m03(openMatrix4f.m03);

        jomlMatrix.m10(openMatrix4f.m10);
        jomlMatrix.m11(openMatrix4f.m11);
        jomlMatrix.m12(openMatrix4f.m12);
        jomlMatrix.m13(openMatrix4f.m13);

        jomlMatrix.m20(openMatrix4f.m20);
        jomlMatrix.m21(openMatrix4f.m21);
        jomlMatrix.m22(openMatrix4f.m22);
        jomlMatrix.m23(openMatrix4f.m23);

        jomlMatrix.m30(openMatrix4f.m30);
        jomlMatrix.m31(openMatrix4f.m31);
        jomlMatrix.m32(openMatrix4f.m32);
        jomlMatrix.m33(openMatrix4f.m33);

        return jomlMatrix;
    }
}
