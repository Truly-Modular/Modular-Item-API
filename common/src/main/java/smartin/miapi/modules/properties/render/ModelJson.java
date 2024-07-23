package smartin.miapi.modules.properties.render;

import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import smartin.miapi.item.modular.Transform;

public class ModelJson {
    public String type = "type";
    public String model = "path";
    @CodecBehavior.Optional
    public String modelType = "default";
    @CodecBehavior.Optional
    public Transform transform = Transform.IDENTITY;
}
