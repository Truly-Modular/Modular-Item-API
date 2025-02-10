package smartin.miapi.material.composite.material;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.material.DefaultMaterial;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.composite.Composite;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public interface CompositeFromOtherMaterial extends Composite {
    Map<ResourceLocation, MapCodec<? extends CompositeFromOtherMaterial>> MATERIAL_BASE_COMPOSITE_REGISTRY = new HashMap<>();

    static void register(ResourceLocation id, MapCodec<? extends CompositeFromOtherMaterial> codec) {
        MATERIAL_BASE_COMPOSITE_REGISTRY.put(id, codec);
        Composite.COMPOSITE_REGISTRY.put(id, getCodec(codec));
    }


    static <T extends CompositeFromOtherMaterial> MapCodec<T> getCodec(MapCodec<T> base) {
        return RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        base.forGetter(t -> t), // Retain existing fields
                        MaterialProperty.MATERIAL_CODEC.optionalFieldOf("material", new DefaultMaterial())
                                .forGetter(CompositeFromOtherMaterial::getMaterial) // Getter for material
                ).apply(instance, (a, m) -> {
                    a.setMaterial(m);
                    return a;
                })
        );
    }

    static <T extends CompositeFromOtherMaterial> MapCodec<T> getEmptyCodec(Supplier<T> getter) {
        return RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        MaterialProperty.MATERIAL_CODEC.optionalFieldOf("material", new DefaultMaterial())
                                .forGetter(CompositeFromOtherMaterial::getMaterial) // Getter for material
                ).apply(instance, (m) -> {
                    T mat = getter.get();
                    mat.setMaterial(m);
                    return mat;
                })
        );
    }

    static <T extends CompositeFromOtherMaterial> MapCodec<T> getEmptyCodecMaterial(Function<Material, T> getter) {
        return RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        MaterialProperty.MATERIAL_CODEC.optionalFieldOf("material", new DefaultMaterial())
                                .forGetter(CompositeFromOtherMaterial::getMaterial) // Getter for material
                ).apply(instance, (m) -> {
                    T mat = getter.apply(m);
                    mat.setMaterial(m);
                    return mat;
                })
        );
    }

    void setMaterial(Material material);

    Material getMaterial();
}
