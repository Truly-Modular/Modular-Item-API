package smartin.miapi.modules.properties.render;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;

/**
 * @header Emissivity Property
 * @path /data_types/properties/render/emissive
 * @description_start
 * The EmissivityProperty defines the emissive light levels for an item or block. Emissive properties affect how much light
 * an item or block emits.
 * These values are applied to the item or block to control its visual appearance in lighting conditions.
 * @description_end
 * @data sky: An integer representing the light level emitted in the sky.
 * @data block: An integer representing the light level emitted from the block.
 */
public class EmissivityProperty extends CodecProperty<EmissivityProperty.LightJson> {
    public static final ResourceLocation KEY = Miapi.id("emissive");
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
        public int sky = -1;
        public int block = -1;

        public int[] asArray() {
            return new int[]{sky, block};
        }
    }
}
