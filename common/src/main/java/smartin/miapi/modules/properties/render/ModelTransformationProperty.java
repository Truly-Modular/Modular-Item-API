package smartin.miapi.modules.properties.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class ModelTransformationProperty extends CodecProperty<ModelTransformationProperty.ModelTransformationData> {
    public static final ResourceLocation KEY = Miapi.id("model_transform");
    public static ModelTransformationProperty property;

    public ModelTransformationProperty() {
        super(AutoCodec.of(ModelTransformationData.class).codec());
        property = this;
        ModularItemCache.setSupplier(KEY.toString(), ModelTransformationProperty::getTransformation);
        MiapiItemModel.modelTransformers.add((matrices, itemStack, mode, modelType, tickDelta) -> {
            applyTransformation(itemStack, mode, matrices);
            return matrices;
        });
    }

    public static void applyTransformation(ItemStack stack, ItemDisplayContext mode, PoseStack matrices) {
        var data = ModularItemCache.getVisualOnlyCache(stack, KEY.toString(), ItemTransforms.NO_TRANSFORMS);
        ItemTransform transformation = data.getTransform(mode);
        boolean leftHanded = isLeftHanded(mode);
        matrices.translate(0.5f, 0.5f, 0.5f);
        if (transformation != null) {
            transformation.apply(false, matrices);
        }
        matrices.translate(-0.5f, -0.5f, -0.5f);
    }

    public static ItemTransforms getTransformation(ItemStack stack) {
        return property.getData(stack).orElseGet(ModelTransformationData::new).asItemTransforms();
    }

    public static boolean isLeftHanded(ItemDisplayContext mode) {
        return switch (mode) {
            case THIRD_PERSON_LEFT_HAND, FIRST_PERSON_LEFT_HAND -> true;
            default -> false;
        };
    }

    //TODO:requires testing and more debugging with makeLeft vs not make left
    public static Transform makeLeft(Transform transform) {
        transform = transform.copy();
        transform.translation.set(new Vector3f(-transform.translation.x(), transform.translation.y(), transform.translation.z()));
        Matrix4f m = transform.toMatrix();
        m.rotate((float) Math.PI, new Vector3f(1, 0, 0));
        m.rotate((float) Math.PI, new Vector3f(0, 1, 0));
        m.rotate((float) Math.PI, new Vector3f(0, 0, 1));
        Transform rotated = Transform.fromMatrix(m);
        rotated.origin = transform.origin;

        return rotated;
    }

    private static Set<String> getStringOfMode(ItemDisplayContext mode) {
        List<String> modes = new ArrayList<>();
        modes.add(mode.toString());
        modes.add(mode.toString().replace("_", ""));
        modes.add(mode.toString().toLowerCase());
        modes.add(mode.toString().toLowerCase().replace("_", ""));
        switch (mode) {
            case GUI -> modes.add("gui");
            case HEAD -> modes.add("head");
            case FIXED -> modes.add("fixed");
            case GROUND -> modes.add("ground");
            case FIRST_PERSON_LEFT_HAND -> modes.add("firstperson_lefthand");
            case FIRST_PERSON_RIGHT_HAND -> modes.add("firstperson_righthand");
            case THIRD_PERSON_LEFT_HAND -> modes.add("thirdperson_lefthand");
            case THIRD_PERSON_RIGHT_HAND -> modes.add("thirdperson_righthand");
        }
        return Set.copyOf(modes);
    }

    @Override
    public ModelTransformationData merge(ModelTransformationData left, ModelTransformationData right, MergeType mergeType) {
        return ModelTransformationData.merge(left, right, mergeType);
    }

    public static class ModelTransformationData {
        @CodecBehavior.Optional
        public Transform gui = null;
        @CodecBehavior.Optional
        public Transform head = null;
        @CodecBehavior.Optional
        public Transform fixed = null;
        @CodecBehavior.Optional
        public Transform ground = null;
        @CodecBehavior.Optional
        @AutoCodec.Name("firstperson_lefthand")
        public Transform firstPersonLeftHand = null;
        @CodecBehavior.Optional
        @AutoCodec.Name("firstperson_righthand")
        public Transform firstPersonRightHand = null;
        @CodecBehavior.Optional
        @AutoCodec.Name("thirdperson_lefthand")
        public Transform thirdPersonLeftHand = null;
        @CodecBehavior.Optional
        @AutoCodec.Name("thirdperson_righthand")
        public Transform thirdPersonRightHand = null;
        @CodecBehavior.Optional
        public boolean overwrite = true;

        public static ModelTransformationData merge(ModelTransformationData left, ModelTransformationData right, MergeType mergeType) {
            ModelTransformationData data = new ModelTransformationData();

            if (right.overwrite) {
                data.gui = overwrite(left.gui, right.gui);
                data.head = overwrite(left.head, right.head);
                data.fixed = overwrite(left.fixed, right.fixed);
                data.ground = overwrite(left.ground, right.ground);
                data.firstPersonLeftHand = overwrite(left.firstPersonLeftHand, right.firstPersonLeftHand);
                data.firstPersonRightHand = overwrite(left.firstPersonRightHand, right.firstPersonRightHand);
                data.thirdPersonLeftHand = overwrite(left.thirdPersonLeftHand, right.thirdPersonLeftHand);
                data.thirdPersonRightHand = overwrite(left.thirdPersonRightHand, right.thirdPersonRightHand);
            } else {
                data.gui = merge(left.gui, right.gui);
                data.head = merge(left.head, right.head);
                data.fixed = merge(left.fixed, right.fixed);
                data.ground = merge(left.ground, right.ground);
                data.firstPersonLeftHand = merge(left.firstPersonLeftHand, right.firstPersonLeftHand);
                data.firstPersonRightHand = merge(left.firstPersonRightHand, right.firstPersonRightHand);
                data.thirdPersonLeftHand = merge(left.thirdPersonLeftHand, right.thirdPersonLeftHand);
                data.thirdPersonRightHand = merge(left.thirdPersonRightHand, right.thirdPersonRightHand);
            }

            return data;
        }

        public static Transform merge(@Nullable Transform left, @Nullable Transform right) {
            if (left == null && right == null) {
                return null;
            }
            if (left != null && right == null) {
                return left.copy();
            }
            if (left == null && right != null) {
                return right.copy();
            }
            return Transform.merge(left, right);
        }

        public static Transform overwrite(@Nullable Transform left, @Nullable Transform right) {
            if (right == null) {
                return left != null ? left.copy() : null;
            }
            return right.copy();
        }

        public ItemTransforms asItemTransforms() {
            return new ItemTransforms(
                    thirdPersonLeftHand != null ? thirdPersonLeftHand.toTransformation() : Transform.IDENTITY.toTransformation(),
                    thirdPersonRightHand != null ? thirdPersonRightHand.toTransformation() : Transform.IDENTITY.toTransformation(),
                    firstPersonLeftHand != null ? firstPersonLeftHand.toTransformation() : Transform.IDENTITY.toTransformation(),
                    firstPersonRightHand != null ? firstPersonRightHand.toTransformation() : Transform.IDENTITY.toTransformation(),
                    head != null ? head.toTransformation() : Transform.IDENTITY.toTransformation(),
                    gui != null ? gui.toTransformation() : Transform.IDENTITY.toTransformation(),
                    ground != null ? ground.toTransformation() : Transform.IDENTITY.toTransformation(),
                    fixed != null ? fixed.toTransformation() : Transform.IDENTITY.toTransformation()
            );
        }

    }
}
