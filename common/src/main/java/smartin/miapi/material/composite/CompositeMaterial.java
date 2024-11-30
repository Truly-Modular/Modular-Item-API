package smartin.miapi.material.composite;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.material.DefaultMaterial;
import smartin.miapi.material.DelegatingMaterial;
import smartin.miapi.material.Material;

import java.util.List;
import java.util.Optional;

public class CompositeMaterial extends DelegatingMaterial {
    public static ResourceLocation KEY = Miapi.id("composite_material");
    public List<Composite> composites;
    public double cost = 1.0;
    public static MapCodec<CompositeMaterial> CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    Composite.CODEC
                            .listOf()
                            .optionalFieldOf("composites", List.of())
                            .forGetter((material) -> material.composites)
            ).apply(instance, (composites) -> {
                Material buildMaterial = new DefaultMaterial();
                for (Composite composite : composites) {
                    buildMaterial = composite.composite(buildMaterial, Environment.isClient());
                }
                return new CompositeMaterial(buildMaterial, composites);
            }));

    public static DataComponentType<CompositeMaterial> COMPOSITE_MATERIAL_COMPONENT = DataComponentType.<CompositeMaterial>builder()
            .persistent(CODEC.codec())
            .networkSynchronized(ByteBufCodecs.fromCodec(CODEC.codec())).build();

    public CompositeMaterial(Material buildMaterial, List<Composite> composites) {
        super(buildMaterial);
        this.composites = composites;
    }

    @Override
    public ResourceLocation getID() {
        return KEY;
    }

    @Override
    public Optional<MapCodec<? extends Material>> codec() {
        return Optional.of(CODEC);
    }
}
