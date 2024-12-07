package smartin.miapi.material.composite;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.material.DelegatingMaterial;
import smartin.miapi.material.Material;

import java.util.Map;

public record PropertyAdjustComponent(Map<String, Double> stats) implements Composite {
    public static final ResourceLocation ID = Miapi.id("set_property");
    public static final MapCodec<PropertyAdjustComponent> codec = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    Codec.unboundedMap(Codec.STRING, Codec.DOUBLE)
                            .optionalFieldOf("stats", Map.of())
                            .forGetter(PropertyAdjustComponent::stats)
            ).apply(instance, PropertyAdjustComponent::new));

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
}
