package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.datapack.codec.AutoCodec;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.CodecBasedProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;

public class QuarryProperty extends CodecBasedProperty<List<QuarryProperty.Holder>> {
    public static final String KEY = "quarry";
    public static QuarryProperty property;
    public static final Codec<List<Holder>> codec = AutoCodec.of(Holder.class).codec().listOf();

    public QuarryProperty() {
        super(KEY, codec);
        property = this;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        return ModuleProperty.mergeList(old, toMerge, type);
    }

    public static class Holder {

    }
}
