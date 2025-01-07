package smartin.miapi.forge.compat.epic_fight;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import smartin.miapi.client.model.MiapiItemModel;
import yesman.epicfight.api.client.model.MeshProvider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.patched.layer.ModelRenderLayer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class CustomLivingArmorRenderer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends BipedEntityModel<E>, AM extends HumanoidMesh> extends ModelRenderLayer<E, T, M, ArmorFeatureRenderer<E, M, M>, AM> {
    public CustomLivingArmorRenderer(MeshProvider mesh) {
        super(mesh);
        addEFModelProvider(new String[]{"Root", "Torso", "Chest", "Shoulder_R", "Arm_R"}, "right_arm");
        addEFModelProvider(new String[]{"Root", "Torso", "Chest", "Shoulder_L", "Arm_L"}, "left_arm");
        addEFModelProvider(new String[]{"Root", "Thigh_R", "Leg_R"}, "right_leg");
        addEFModelProvider(new String[]{"Root", "Thigh_R", "Leg_L"}, "left_leg");
        addEFModelProvider(new String[]{"Root", "Torso", "Chest", "Head"}, "head");
        addEFModelProvider(new String[]{"Root", "Torso", "Chest", "Head"}, "hat");
        addEFModelProvider(new String[]{"Root", "Torso", "Chest"}, "chest");
        addEFModelProvider(new String[]{"Root", "Torso", "Chest"}, "body");
    }

    private void addEFModelProvider(String[] joint, String miapiJoint) {
        epicFightModelProviders.add(new ModelProvider(joint, miapiJoint));
    }

    List<ModelProvider> epicFightModelProviders = new ArrayList<>();

    @Override
    protected void renderLayer(T patch, E entity, @Nullable ArmorFeatureRenderer<E, M, M> emmArmorFeatureRenderer, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, OpenMatrix4f[] openMatrix4fs, float v, float v1, float v2, float v3) {
        renderSlot(entity, patch, emmArmorFeatureRenderer, matrixStack, vertexConsumerProvider, i, EquipmentSlot.HEAD, openMatrix4fs);
        renderSlot(entity, patch, emmArmorFeatureRenderer, matrixStack, vertexConsumerProvider, i, EquipmentSlot.CHEST, openMatrix4fs);
        renderSlot(entity, patch, emmArmorFeatureRenderer, matrixStack, vertexConsumerProvider, i, EquipmentSlot.LEGS, openMatrix4fs);
        renderSlot(entity, patch, emmArmorFeatureRenderer, matrixStack, vertexConsumerProvider, i, EquipmentSlot.FEET, openMatrix4fs);

    }

    private void renderSlot(E entity, T patch, @NotNull ArmorFeatureRenderer<E, M, M> emmArmorFeatureRenderer, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, EquipmentSlot slot, OpenMatrix4f[] openMatrix4fs) {
        renderArmorPiece(matrixStack, vertexConsumerProvider, i, slot, entity.getEquippedStack(slot), entity, patch, openMatrix4fs);
    }

    public void renderArmorPiece(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, EquipmentSlot armorSlot, ItemStack itemStack, LivingEntity entity, T patch, OpenMatrix4f[] openMatrix4fs) {
        matrices.push();
        //matrices.multiplyPositionMatrix(toJomlMatrix(patch.getArmature().getRootJoint().getToOrigin()));
        epicFightModelProviders.forEach(modelProvider -> {
            matrices.push();
            if (modelProvider.apply(matrices, patch.getArmature())) {
                String key = modelProvider.tmId;
                MiapiItemModel miapiItemModel = MiapiItemModel.getItemModel(itemStack);
                //matrices.multiplyPositionMatrix(toJomlMatrix(openMatrix4fs[patch.getArmature().searchPathIndex("Head")]));
                //patch.getArmature().searchJointByName(modelProvider.efId).getToOrigin()
                for (int i = modelProvider.efId.length-1; i <= 0; i++) {
                    var transform = patch.getClientAnimator().getPose(0.0f).getOrDefaultTransform(modelProvider.efId[i]);
                    matrices.multiplyPositionMatrix(toJomlMatrix(patch.getClientAnimator().getPose(0.0f).getOrDefaultTransform(modelProvider.efId[i]).toMatrix()));
                }
                //matrices.multiplyPositionMatrix(toJomlMatrix(patch.getAnimator().getPose(0.0f).getOrDefaultTransform(modelProvider.efId).toMatrix()));

                if (miapiItemModel != null) {
                    miapiItemModel.render(key, matrices, ModelTransformationMode.HEAD, 0, vertexConsumers, entity, light, OverlayTexture.DEFAULT_UV);
                }
            }
            matrices.pop();
        });
        matrices.pop();
    }

    public class ModelProvider {
        public String tmId;
        public String[] efId = new String[]{};

        public ModelProvider(String[] jointID, String tmId) {
            this.efId = jointID;
            this.tmId = tmId;
        }

        public boolean apply(MatrixStack matrixStack, Armature armature) {
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
