package smartin.miapi.modules.properties;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.material.AllowedMaterial;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * @header Module Stats Property
 * @path /data_types/properties/module_stats
 * @description_start
 * The ModuleStats property allows for the specification of various statistics associated with a module, where each statistic
 * is represented by a key-value pair.
 * This property is integrated with the Stat Resolver and can be queried by using [module.custom_stat_name].
 *
 * @description_end
 * @data stats: A {@link Map} where each entry consists of a {@link String} key and a {@link Double} value, representing different
 * statistics related to the module. The statistics can include metrics like "cost" and other module-specific data.
 */

public class ModuleStats extends CodecProperty<Map<String, Double>> {
    public static final ResourceLocation KEY = Miapi.id("module_stats");
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
        return ModuleProperty.mergeMap(left,right,mergeType);
    }
}
