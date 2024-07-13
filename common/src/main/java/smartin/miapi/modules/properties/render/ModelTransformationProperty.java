package smartin.miapi.modules.properties.render;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.*;

@Environment(EnvType.CLIENT)
public class ModelTransformationProperty extends CodecProperty<List<ModelTransformationProperty.ModelTransformationData>> {

    public static final String KEY = "modelTransform";
    public static ModelTransformationProperty property;

    public ModelTransformationProperty() {
        super(Codec.list(AutoCodec.of(ModelTransformationData.class).codec()));
        property = this;
        ModularItemCache.setSupplier(KEY, ModelTransformationProperty::getTransformation);
        MiapiItemModel.modelTransformers.add((matrices, itemStack, mode, modelType, tickDelta) -> {
            applyTransformation(itemStack, mode, matrices);
            return matrices;
        });
    }

    public static void applyTransformation(ItemStack stack, ItemDisplayContext mode, PoseStack matrices) {
        ItemTransform transformation = ModularItemCache.getVisualOnlyCache(stack, KEY, ItemTransforms.NO_TRANSFORMS).getTransform(mode);
        boolean leftHanded = isLeftHanded(mode);
        matrices.translate(0.5f, 0.5f, 0.5f);
        if (transformation != null) {
            transformation.apply(false, matrices);
        }
        matrices.translate(-0.5f, -0.5f, -0.5f);
    }

    public static ItemTransforms getTransformation(ItemStack stack) {
        ItemTransforms transformation = ItemTransforms.NO_TRANSFORMS;
        for (ModuleInstance instance : ItemModule.createFlatList(ItemModule.getModules(stack))) {
            JsonElement element = instance.getOldProperties().get(property);
            if (element != null) {
                Map<ItemDisplayContext, ItemTransform> map = new HashMap<>();
                if (element.getAsJsonObject().has("replace")) {
                    JsonObject replace = element.getAsJsonObject().getAsJsonObject("replace");
                    for (ItemDisplayContext mode : ItemDisplayContext.values()) {
                        map.put(mode, transformation.getTransform(mode));
                        for (String modeString : getStringOfMode(mode)) {
                            if (replace.has(modeString)) {
                                Transform transform = Transform.toModelTransformation(Miapi.gson.fromJson(replace.getAsJsonObject(modeString), Transform.class));
                                map.put(mode, transform.toTransformation());
                            }
                        }
                    }
                }
                if (element.getAsJsonObject().has("display")) {
                    JsonObject replace = element.getAsJsonObject().getAsJsonObject("display");
                    for (ItemDisplayContext mode : ItemDisplayContext.values()) {
                        map.put(mode, transformation.getTransform(mode));
                        for (String modeString : getStringOfMode(mode)) {
                            if (replace.has(modeString)) {
                                Transform transform = Transform.toModelTransformation(Miapi.gson.fromJson(replace.getAsJsonObject(modeString), Transform.class));
                                if (isLeftHanded(mode)) {
                                    transform = makeLeft(transform);
                                }
                                map.put(mode, transform.toTransformation());
                            }
                        }
                    }
                }
                if (element.getAsJsonObject().has("display-merge")) {
                    JsonObject replace = element.getAsJsonObject().getAsJsonObject("display-merge");
                    for (ItemDisplayContext mode : ItemDisplayContext.values()) {
                        map.put(mode, transformation.getTransform(mode));
                        for (String modeString : getStringOfMode(mode)) {
                            if (replace.has(modeString)) {
                                Transform merged = Transform.merge(new Transform(transformation.getTransform(mode)), Miapi.gson.fromJson(replace.getAsJsonObject(modeString), Transform.class));
                                if (isLeftHanded(mode)) {
                                    merged = makeLeft(merged);
                                }
                                map.put(mode, merged.toTransformation());
                            }
                        }
                    }
                }
                if (element.getAsJsonObject().has("merge")) {
                    JsonObject replace = element.getAsJsonObject().getAsJsonObject("merge");
                    for (ItemDisplayContext mode : ItemDisplayContext.values()) {
                        map.put(mode, transformation.getTransform(mode));
                        for (String modeString : getStringOfMode(mode)) {
                            if (replace.has(modeString)) {
                                Transform merged = Transform.merge(new Transform(transformation.getTransform(mode)), Miapi.gson.fromJson(replace.getAsJsonObject(modeString), Transform.class));
                                map.put(mode, merged.toTransformation());
                            }
                        }
                    }
                }
                transformation = new ItemTransforms(
                        map.get(ItemDisplayContext.THIRD_PERSON_LEFT_HAND),
                        map.get(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND),
                        map.get(ItemDisplayContext.FIRST_PERSON_LEFT_HAND),
                        map.get(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND),
                        map.get(ItemDisplayContext.HEAD),
                        map.get(ItemDisplayContext.GUI),
                        map.get(ItemDisplayContext.GROUND),
                        map.get(ItemDisplayContext.FIXED)
                );
            }
        }
        return transformation;
    }

    public static boolean isLeftHanded(ItemDisplayContext mode) {
        return switch (mode) {
            case THIRD_PERSON_LEFT_HAND, FIRST_PERSON_LEFT_HAND -> true;
            default -> false;
        };
    }

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

    public class ModelTransformationData {
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
    }
}
