package smartin.miapi.material.composite.material;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.craft.stat.StatProvidersMap;
import smartin.miapi.material.DelegatingMaterial;
import smartin.miapi.material.base.Material;

import java.util.Objects;

/**
 * This Composite allows a material to be replaced with another material.
 *
 * @header Material Copy Composite
 * @description_start The Material Copy Composite replaces the current material with a specified base material.
 * This allows for direct substitution of one material with another, maintaining all properties
 * and behaviors of the target material.
 * @description_end
 * @path /data_types/composites/material_copy
 * @data material: The material that replaces the current material.
 */
public class MaterialMergeStatComposite extends BasicOtherMaterialComposite {
    public static ResourceLocation ID = Miapi.id("material_merge_stat");
    public static MapCodec<MaterialMergeStatComposite> INNER_MAP_CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    StatProvidersMap.Codec.DOUBLE.fieldOf("ratio").forGetter(m -> m.ratio)
            ).apply(instance, MaterialMergeStatComposite::new));
    public static MapCodec<MaterialMergeStatComposite> MAP_CODEC = CompositeFromOtherMaterial.getCodec(INNER_MAP_CODEC);
    public double ratio;

    public MaterialMergeStatComposite(double ratio) {
        super();
        this.ratio = ratio;
    }

    @Override
    public Material composite(Material parent, boolean isClient) {
        return new DelegatingMaterial(parent) {
            @Override
            public double getDouble(String property) {
                return parent.getDouble(property) * ratio + material.getDouble(property) * (1 - ratio);
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
        MaterialMergeStatComposite that = (MaterialMergeStatComposite) obj;
        return overWriteAble == that.overWriteAble &&
               Objects.equals(material, that.material) &&
               Objects.equals(ratio, ratio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, overWriteAble, ratio);
    }
}
