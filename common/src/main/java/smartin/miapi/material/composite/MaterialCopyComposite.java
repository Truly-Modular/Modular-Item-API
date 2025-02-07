package smartin.miapi.material.composite;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.MaterialProperty;

/**
 * This Composite allows a material to be replaced with another material.
 *
 * @header Material Copy Composite
 * @description_start
 * The Material Copy Composite replaces the current material with a specified base material.
 * This allows for direct substitution of one material with another, maintaining all properties
 * and behaviors of the target material.
 * @description_end
 * @path /data_types/composites/material_copy
 * @data base: The material that replaces the current material.
 */
public record MaterialCopyComposite(Material material) implements Composite {
    public static ResourceLocation ID = Miapi.id("material_replace");
    public static MapCodec<Composite> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    Miapi.ID_CODEC.fieldOf("base").forGetter((composite -> {
                        if(composite instanceof MaterialCopyComposite materialCopyComposite){
                            return materialCopyComposite.material().getID();
                        }
                        return null;
                    }))
            ).apply(instance, MaterialCopyComposite::fromID));
    @Override
    public Material composite(Material parent, boolean isClient) {
        return this.material();
    }

    public static MaterialCopyComposite fromID(ResourceLocation id) {
        return new MaterialCopyComposite(MaterialProperty.materials.get(id));
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }
}
