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
import java.util.Objects;

/**
 * This Composite modifies specific stats by applying a percentage increase to the existing values.
 *
 * @header Percent Stats Composite
 * @description_start
 * The PercentStatComposite allows you to modify specific stats by providing a map of stat names and their respective percentage values.
 * The values are multiplied with the existing stats of the parent material. If the stat is not found in the map, the parent's stat value is used.
 * @description_end
 * @path /data_types/composites/percent_stats
 * @data stats: A map of stat names (String) and their percentage increase values (Double) to apply to the parent material.
 */
public record PercentStatComposite(Map<String, Double> stats) implements Composite {
    public static final ResourceLocation ID = Miapi.id("percent_stat");
    public static final MapCodec<PercentStatComposite> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    Codec.unboundedMap(Codec.STRING, Codec.DOUBLE)
                            .optionalFieldOf("stats", Map.of())
                            .forGetter(PercentStatComposite::stats)
            ).apply(instance, PercentStatComposite::new));

    @Override
    public Material composite(Material parent, boolean isClient) {
        return new DelegatingMaterial(parent) {
            public double getDouble(String property) {
                if (stats.containsKey(property)) {
                    return stats.get(property) * parent.getDouble(property);
                }
                return parent.getDouble(property);
            }
        };
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PercentStatComposite that = (PercentStatComposite) obj;
        return Objects.equals(stats, that.stats);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stats);
    }

}
