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

public record IncreaseStatsComposite(Map<String, Double> stats) implements Composite {
    public static final ResourceLocation ID = Miapi.id("multiply_stat");
    public static final MapCodec<IncreaseStatsComposite> codec = RecordCodecBuilder.mapCodec((instance) ->
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
