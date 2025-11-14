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
 * its type is "miapi:set_stat"
 * sets material number stats.
 * @description_end
 * @path /datapack/material/composites/set_stats
 * @data stats: A map of stat names (String) and their values (Double) to apply.
 */
public record SetStatComposite(Map<String, Double> stats) implements Composite {
    public static final ResourceLocation ID = Miapi.id("set_stat");
    public static final MapCodec<SetStatComposite> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    Codec.unboundedMap(Codec.STRING, Codec.DOUBLE)
                            .optionalFieldOf("stats", Map.of())
                            .forGetter(SetStatComposite::stats)
            ).apply(instance, SetStatComposite::new));

    @Override
    public Material composite(Material parent, boolean isClient) {
        return new DelegatingMaterial(parent) {
            public double getDouble(String property) {
                if (stats.containsKey(property)) {
                    return stats.get(property);
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
        SetStatComposite that = (SetStatComposite) obj;
        return Objects.equals(stats, that.stats);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stats);
    }

}
