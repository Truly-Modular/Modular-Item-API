package smartin.miapi.modules.properties;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;

public class EmissivityProperty extends CodecProperty<EmissivityProperty.LightJson> {
    public static final String KEY = "emissive";
    public static EmissivityProperty property;
    static Codec<LightJson> CODEC = AutoCodec.of(LightJson.class).codec();

    public EmissivityProperty() {
        super(CODEC);
        property = this;
    }

    public static int[] getLightValues(ModuleInstance instance) {
        return property.getData(instance).orElse(new LightJson()).asArray();
    }

    @Override
    public LightJson merge(LightJson left, LightJson right, MergeType mergeType) {
        if (mergeType.equals(MergeType.EXTEND)) {
            return left;
        }
        return right;
    }

    public static class LightJson {
        public int sky = 15;
        public int block = 15;

        int[] asArray() {
            return new int[]{sky, block};
        }
    }
}
