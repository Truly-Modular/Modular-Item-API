package smartin.miapi.material.composite.material;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
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
public class MaterialCopyComposite extends BasicOtherMaterialComposite {
    public static ResourceLocation ID = Miapi.id("material_replace");
    public static MapCodec<MaterialCopyComposite> MAP_CODEC = CompositeFromOtherMaterial.getEmptyCodecMaterial(MaterialCopyComposite::new);

    public MaterialCopyComposite(Material material) {
        super(material);
    }

    @Override
    public Material composite(Material parent, boolean isClient) {
        return new DelegatingMaterial(material);
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MaterialCopyComposite that = (MaterialCopyComposite) obj;
        return overWriteAble == that.overWriteAble &&
               Objects.equals(material, that.material);
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, overWriteAble);
    }
}
