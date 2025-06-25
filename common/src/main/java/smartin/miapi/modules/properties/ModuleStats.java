package smartin.miapi.modules.properties;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.material.AllowedMaterial;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.HashMap;
import java.util.Map;

/**
 * @header Module Stats Property
 * @path /data_types/properties/module_stats
 * @description_start The ModuleStats property allows for the specification of various statistics associated with a module, where each statistic
 * is represented by a key-value pair.
 * This property is integrated with the Stat Resolver and can be queried by using [module.custom_stat_name].
 * @description_end
 * @data stats: A {@link Map} where each entry consists of a {@link String} key and a {@link Double} value, representing different
 * statistics related to the module. The statistics can include metrics like "cost" and other module-specific data.
 */

public class ModuleStats extends CodecProperty<Map<String, DoubleOperationResolvable>> {
    public static final ResourceLocation KEY = Miapi.id("module_stats");
    public static ModuleStats property;
    public static Codec<Map<String, DoubleOperationResolvable>> CODEC = Codec.dispatchedMap(Codec.STRING, (s) -> DoubleOperationResolvable.CODEC);

    public ModuleStats() {
        super(CODEC);
        property = this;
        StatResolver.registerResolver("module", (data, instance) -> {
            if (instance.getModule().equals(ItemModule.internal)) {
                return 1.0;
            }
            if ("cost".equals(data)) {
                return AllowedMaterial.getMaterialCost(instance);
            }
            DoubleOperationResolvable resolvable = getData(instance).orElse(new HashMap<>()).get(data);
            if (resolvable != null) {
                return resolvable.getValue();
            } else {
                return 0;
            }
        });
    }

    @Override
    public Map<String, DoubleOperationResolvable> merge(Map<String, DoubleOperationResolvable> left, Map<String, DoubleOperationResolvable> right, MergeType mergeType) {
        return MergeAble.mergeMap(left, right, mergeType);
    }

    public Map<String, DoubleOperationResolvable> initialize(Map<String, DoubleOperationResolvable> data, ModuleInstance moduleInstance) {
        Map<String, DoubleOperationResolvable> map = new HashMap<>();
        data.forEach((id, value) -> {
            map.put(id, value.initialize(moduleInstance));
        });
        return map;
    }
}
