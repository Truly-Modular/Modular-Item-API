package smartin.miapi.modules.properties.render;

import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import smartin.miapi.item.modular.Transform;

public class ModelJson {
    public String type;
    public String model;
    @CodecBehavior.Optional
    public String modelType;
    @CodecBehavior.Optional
    public Transform transform = Transform.IDENTITY;
}
