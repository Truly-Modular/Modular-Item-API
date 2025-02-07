package smartin.miapi.material.composite.stat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.material.DelegatingMaterial;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.composite.Composite;

import java.util.Map;

/**
 * This Composite increases specific stats by adding the given values to the existing ones.
 *
 * @header Add Stats Composite
 * @description_start
 * The IncreaseStatsComposite allows you to increase specific stats by providing a map of stat names and their respective values.
 * The values are added to the existing stats of the parent material. If the stat is not found in the map, the parent's stat value is used.
 * @description_end
 * @path /data_types/composites/increase_stats
 * @data stats: A map of stat names (String) and their increase values (Double) to apply to the parent material.
 */
public record IncreaseStatsComposite(Map<String, Double> stats) implements Composite {
    public static final ResourceLocation ID = Miapi.id("add_stat");
    public static final MapCodec<IncreaseStatsComposite> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    Codec.unboundedMap(Codec.STRING, Codec.DOUBLE)
                            .optionalFieldOf("stats", Map.of())
                            .forGetter(IncreaseStatsComposite::stats)
            ).apply(instance, IncreaseStatsComposite::new));

    @Override
    public Material composite(Material parent, boolean isClient) {
        return new DelegatingMaterial(parent) {
            public double getDouble(String property) {
                if (stats.containsKey(property)) {
                    return stats.get(property) + parent.getDouble(property);
                }
                return parent.getDouble(property);
            }
        };
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }
}
