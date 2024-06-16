package smartin.miapi.modules.properties.render;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.*;

@Environment(EnvType.CLIENT)
public class ModelTransformationProperty implements RenderProperty {

    public static final String KEY = "modelTransform";
    public static ModuleProperty property;

    public ModelTransformationProperty() {
        property = this;
        ModularItemCache.setSupplier(KEY, ModelTransformationProperty::getTransformation);
        MiapiItemModel.modelTransformers.add((matrices, itemStack, mode, modelType, tickDelta) -> {
            applyTransformation(itemStack, mode, matrices);
            return matrices;
        });
    }

    public static void applyTransformation(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices) {
        Transformation transformation = ModularItemCache.getVisualOnlyCache(stack, KEY, ModelTransformation.NONE).getTransformation(mode);
        boolean leftHanded = isLeftHanded(mode);
        matrices.translate(0.5f, 0.5f, 0.5f);
        if(transformation!=null){
            transformation.apply(false, matrices);
        }
        matrices.translate(-0.5f, -0.5f, -0.5f);
    }

    public static ModelTransformation getTransformation(ItemStack stack) {
        ModelTransformation transformation = ModelTransformation.NONE;
        for (ModuleInstance instance : ItemModule.createFlatList(ItemModule.getModules(stack))) {
            JsonElement element = instance.getProperties().get(property);
            if (element != null) {
                Map<ModelTransformationMode, Transformation> map = new HashMap<>();
                if (element.getAsJsonObject().has("replace")) {
                    JsonObject replace = element.getAsJsonObject().getAsJsonObject("replace");
                    for (ModelTransformationMode mode : ModelTransformationMode.values()) {
                        map.put(mode, transformation.getTransformation(mode));
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
                    for (ModelTransformationMode mode : ModelTransformationMode.values()) {
                        map.put(mode, transformation.getTransformation(mode));
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
                    for (ModelTransformationMode mode : ModelTransformationMode.values()) {
                        map.put(mode, transformation.getTransformation(mode));
                        for (String modeString : getStringOfMode(mode)) {
                            if (replace.has(modeString)) {
                                Transform merged = Transform.merge(new Transform(transformation.getTransformation(mode)), Miapi.gson.fromJson(replace.getAsJsonObject(modeString), Transform.class));
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
                    for (ModelTransformationMode mode : ModelTransformationMode.values()) {
                        map.put(mode, transformation.getTransformation(mode));
                        for (String modeString : getStringOfMode(mode)) {
                            if (replace.has(modeString)) {
                                Transform merged = Transform.merge(new Transform(transformation.getTransformation(mode)), Miapi.gson.fromJson(replace.getAsJsonObject(modeString), Transform.class));
                                map.put(mode, merged.toTransformation());
                            }
                        }
                    }
                }
                transformation = new ModelTransformation(
                        map.get(ModelTransformationMode.THIRD_PERSON_LEFT_HAND),
                        map.get(ModelTransformationMode.THIRD_PERSON_RIGHT_HAND),
                        map.get(ModelTransformationMode.FIRST_PERSON_LEFT_HAND),
                        map.get(ModelTransformationMode.FIRST_PERSON_RIGHT_HAND),
                        map.get(ModelTransformationMode.HEAD),
                        map.get(ModelTransformationMode.GUI),
                        map.get(ModelTransformationMode.GROUND),
                        map.get(ModelTransformationMode.FIXED)
                );
            }
        }
        return transformation;
    }

    public static boolean isLeftHanded(ModelTransformationMode mode) {
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

    private static Set<String> getStringOfMode(ModelTransformationMode mode) {
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
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }
}
