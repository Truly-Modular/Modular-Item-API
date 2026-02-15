package smartin.miapi.modules.properties.render.baked;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import smartin.miapi.Miapi;
import smartin.miapi.client.renderer.TrimRenderer;
import smartin.miapi.item.modular.Transform;

import java.util.Optional;

public class ModelData {
    public String path;
    public Transform transform = Transform.IDENTITY;
    public String condition = "1";
    public String color_provider = "material";
    public String trim_mode = "none";
    public Boolean entity_render = false;
    public String id = null;

    public static final Codec<ModelData> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    Codec.STRING.fieldOf("path")
                            .forGetter((modelData) -> modelData.path),
                    Transform.CODEC.optionalFieldOf("transform", Transform.IDENTITY)
                            .forGetter((modelData) -> modelData.transform),
                    Codec.STRING.optionalFieldOf("condition", "1")
                            .forGetter((modelData) -> modelData.condition),
                    Codec.STRING.optionalFieldOf("color_provider", "material")
                            .forGetter((modelData) -> modelData.color_provider),
                    Codec.STRING.optionalFieldOf("trim_mode", "none")
                            .forGetter((modelData) -> modelData.trim_mode),
                    Miapi.FIXED_BOOL_CODEC.optionalFieldOf("entity_render")
                            .forGetter((modelData) -> Optional.of(modelData.entity_render)),
                    Codec.STRING.optionalFieldOf("id", "")
                            .forGetter((modelData) -> modelData.id)
            ).apply(instance, ModelData::new)
    );

    // Constructor to work with RecordCodecBuilder
    public ModelData(String path, Transform transform, String condition, String color_provider,
                     String trim_mode, Optional<Boolean> entity_render, String id) {
        this.path = path;
        this.transform = transform;
        this.condition = condition;
        this.color_provider = color_provider;
        this.trim_mode = trim_mode;
        this.entity_render = entity_render.orElseGet(() -> (
                this.getTrimMode().equals(TrimRenderer.TrimMode.ARMOR_LAYER_ONE) ||
                this.getTrimMode().equals(TrimRenderer.TrimMode.ARMOR_LAYER_TWO)));
        this.id = id;
        repair();
    }

    public void repair() {
        //this shouldn't be necessary as the values should be loaded from the class but anyways
        if (transform == null) {
            transform = Transform.IDENTITY;
        }
        transform = Transform.repair(transform);
    }

    public TrimRenderer.TrimMode getTrimMode() {
        if (trim_mode == null) {
            return TrimRenderer.TrimMode.NONE;
        } else {
            return switch (trim_mode.toLowerCase()) {
                case "armor_layer_one" -> TrimRenderer.TrimMode.ARMOR_LAYER_ONE;
                case "armor_layer_two" -> TrimRenderer.TrimMode.ARMOR_LAYER_TWO;
                case "item" -> TrimRenderer.TrimMode.ITEM;
                default -> TrimRenderer.TrimMode.NONE;
            };
        }
    }
}
