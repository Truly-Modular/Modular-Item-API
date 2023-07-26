package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import smartin.miapi.craft.stat.CraftingStat;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.CodecBasedProperty;
import smartin.miapi.modules.properties.util.DynamicCodecBasedProperty;
import smartin.miapi.modules.properties.util.MergeType;

public class StatProvisionProperty extends DynamicCodecBasedProperty<CraftingStat.StatMap<?>, CraftingStat.StatMap<?>> {
    public static final String KEY = "provideStats";
    public static StatProvisionProperty property;

    public StatProvisionProperty() {
        super(KEY);
        property = this;
    }

    @Override
    public void addTo(ItemModule.ModuleInstance module, CraftingStat.StatMap<?> object, CraftingStat.StatMap<?> holder) {
        holder.putAll((CraftingStat.StatMap) object);
    }

    @Override
    public CraftingStat.StatMap<?> createNewHolder() {
        return new CraftingStat.StatMap<>();
    }

    @Override
    public Codec<CraftingStat.StatMap<?>> codec(ItemModule.ModuleInstance instance) {
        return new CraftingStat.StatMap.StatMapCodec(instance);
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        switch (type) {
            case OVERWRITE -> {
                return toMerge;
            }
            case SMART, EXTEND -> {
                JsonObject obj = old.deepCopy().getAsJsonObject();
                toMerge.getAsJsonObject().asMap().forEach(obj::add);
                return obj;
            }
        }
        return old;
    }
}
