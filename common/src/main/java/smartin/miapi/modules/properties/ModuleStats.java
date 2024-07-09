package smartin.miapi.modules.properties;

import com.mojang.serialization.Codec;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.material.AllowedMaterial;
import smartin.miapi.modules.properties.util.CodecBasedProperty;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.HashMap;
import java.util.Map;

public class ModuleStats extends CodecBasedProperty<Map<String, Double>> {
    public static String KEY = "module_stats";
    public static ModuleStats property;
    public static Codec<Map<String, Double>> CODEC = Codec.dispatchedMap(Codec.STRING, (s) -> Codec.DOUBLE);

    public ModuleStats() {
        super(CODEC);
        property = this;
        StatResolver.registerResolver("module", new StatResolver.Resolver() {
            @Override
            public double resolveDouble(String data, ModuleInstance instance) {
                if (instance.module.equals(ItemModule.internal)) {
                    return 1.0;
                }
                if ("cost".equals(data)) {
                    return AllowedMaterial.getMaterialCost(instance);
                }
                return getData(instance).orElse(new HashMap<>()).getOrDefault(data, 0.0);
            }

            @Override
            public String resolveString(String data, ModuleInstance instance) {
                return null;
            }
        });
    }

    @Override
    public Map<String, Double> merge(Map<String, Double> left, Map<String, Double> right, MergeType mergeType) {
        Map<String, Double> merged = new HashMap<>(left);
        merged.putAll(right);
        return merged;
    }
}
