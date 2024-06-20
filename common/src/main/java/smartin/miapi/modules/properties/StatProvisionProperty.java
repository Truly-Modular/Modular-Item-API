package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import smartin.miapi.craft.stat.StatRequirementMap;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.DynamicCodecBasedProperty;
import smartin.miapi.modules.properties.util.MergeType;

public class StatProvisionProperty extends DynamicCodecBasedProperty<StatRequirementMap, StatRequirementMap> {
    public static final String KEY = "provideStats";
    public static StatProvisionProperty property;

    public StatProvisionProperty() {
        super(KEY);
        property = this;
    }

    @Override
    public void addTo(ItemModule.ModuleInstance module, StatRequirementMap object, StatRequirementMap holder) {
        holder.putAll(object);
    }

    @Override
    public StatRequirementMap createNewHolder() {
        return new StatRequirementMap();
    }

    @Override
    public Codec<StatRequirementMap> codec(ItemModule.ModuleInstance instance) {
        return new StatRequirementMap.Codec(instance);
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
