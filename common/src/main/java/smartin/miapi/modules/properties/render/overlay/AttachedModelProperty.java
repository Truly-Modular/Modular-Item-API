package smartin.miapi.modules.properties.render.overlay;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.ModelHolder;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.render.ModelProperty;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Generic abstract base for data-driven model attachments.
 * Each subclass defines its CustomData type <T>, and provides its Codec<T>.
 */
public abstract class AttachedModelProperty<T extends AttachedModelProperty.CustomData>
        extends CodecProperty<List<AttachedModelProperty.ModelPredicate<T>>> {

    protected AttachedModelProperty(MapCodec<T> dataCodec) {
        super(Codec.list(ModelPredicate.codec(dataCodec)));

        MiapiItemModel.modelSuppliers.add((key, context, module, stack) -> {
            List<MiapiModel> models = new ArrayList<>();

            for (ModuleInstance source : ItemModule.getModules(stack).allSubModules()) {
                for (ModelPredicate<T> predicate : getData(source).orElse(List.of())) {
                    List<ModelProperty.ModelData> modelList =
                            ModelProperty.property.getData(module).orElse(List.of());

                    for (ModelProperty.ModelData modelJson : modelList) {
                        if (predicate.isValid(modelJson)) {
                            ModelHolder baseHolder = ModelProperty.bakedModel(module, modelJson, stack, key);
                            if (baseHolder != null) {
                                List<MiapiModel> model = predicate.data.createModel(stack, module, source, baseHolder);
                                if (model != null)
                                    models.addAll(model);
                            }
                        }
                    }
                }
            }
            return models;
        });
    }

    /**
     * try ensuring the textures/depends are properly loaded.
     */
    @Override
    public boolean load(ResourceLocation id, JsonElement element, boolean isClient) throws Exception {
        List<ModelPredicate<T>> list = decode(element);
        for (ModelPredicate<T> entry : list) {
            entry.preload();
        }
        return isClient;
    }

    @Override
    public List<ModelPredicate<T>> merge(List<ModelPredicate<T>> left, List<ModelPredicate<T>> right, MergeType mergeType) {
        return MergeAble.mergeList(left, right, mergeType);
    }

    /**
     * Base class for custom attachment data.
     */
    public static abstract class CustomData {
        /**
         * Attempt to preload data like models or textures - this might need some more work to ensure for runtime property injection.
         */
        public void preload() {}

        /**
         * @return Creates new models based on existing Models
         */
        @Nullable
        public abstract List<MiapiModel> createModel(ItemStack stack, ModuleInstance base, ModuleInstance source, ModelHolder holder);
    }

    /**
     * Combines matching logic (target pattern, type, etc.) with a CustomData instance.
     */
    public static class ModelPredicate<T extends CustomData> {
        public static <T extends CustomData> Codec<ModelPredicate<T>> codec(MapCodec<T> dataCodec) {
            return RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.STRING.fieldOf("modelTargetType").forGetter(p -> p.modelTargetType),
                            Codec.STRING.fieldOf("modelTargetInfo").forGetter(p -> p.modelTargetInfo),
                            Codec.DOUBLE.optionalFieldOf("priority", 0.0).forGetter(p -> p.priority),
                            Codec.BOOL.optionalFieldOf("allowOtherModules", false).forGetter(p -> p.allowOtherModules),
                            dataCodec.forGetter(p -> p.data)
                    ).apply(instance, ModelPredicate::new)
            );
        }

        public final String modelTargetType;
        public final String modelTargetInfo;
        @CodecBehavior.Optional public final double priority;
        @CodecBehavior.Optional public final boolean allowOtherModules;
        public final T data;

        private final Pattern pattern;

        public ModelPredicate(String modelTargetType, String modelTargetInfo, double priority, boolean allowOtherModules, T data) {
            this.modelTargetType = modelTargetType;
            this.modelTargetInfo = modelTargetInfo;
            this.priority = priority;
            this.allowOtherModules = allowOtherModules;
            this.data = data;
            this.pattern = Pattern.compile(modelTargetInfo);
        }

        public boolean isValid(ModelProperty.ModelData modelJson) {
            return switch (modelTargetType) {
                case "id" -> pattern.matcher(modelJson.id).find();
                case "path" -> pattern.matcher(modelJson.path).find();
                default -> false;
            };
        }

        public void preload() {
            data.preload();
        }
    }
}
