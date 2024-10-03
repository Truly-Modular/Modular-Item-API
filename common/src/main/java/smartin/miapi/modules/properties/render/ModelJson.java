package smartin.miapi.modules.properties.render;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import smartin.miapi.item.modular.Transform;

public class ModelJson {
    public String type = "type";
    @CodecBehavior.Optional
    public String model = "path";
    @CodecBehavior.Optional
    public String modelType = "default";
    @CodecBehavior.Optional
    public Transform transform = Transform.IDENTITY;

    // Codec for ModelJson
    public static final Codec<ModelJson> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    Codec.STRING.fieldOf("type")
                            .forGetter((modelJson) -> modelJson.type),
                    Codec.STRING.optionalFieldOf("model", "path")
                            .forGetter((modelJson) -> modelJson.model),
                    Codec.STRING.optionalFieldOf("modelType", "default")
                            .forGetter((modelJson) -> modelJson.modelType),
                    Transform.CODEC.optionalFieldOf("transform", Transform.IDENTITY)
                            .forGetter((modelJson) -> modelJson.transform)
            ).apply(instance, ModelJson::new)
    );

    // Default constructor for ModelJson to work with apply
    public ModelJson(String type, String model, String modelType, Transform transform) {
        this.type = type;
        this.model = model;
        this.modelType = modelType;
        this.transform = transform;
    }
}
