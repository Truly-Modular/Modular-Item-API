package smartin.miapi.material.composite.material;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import smartin.miapi.Miapi;
import smartin.miapi.material.DelegatingMaterial;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.palette.MaterialRenderController;
import smartin.miapi.modules.ModuleInstance;

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
public class MaterialCopyPaletteComposite extends BasicOtherMaterialComposite {
    public static ResourceLocation ID = Miapi.id("material_palette");
    public static MapCodec<MaterialCopyPaletteComposite> MAP_CODEC = CompositeFromOtherMaterial.getEmptyCodecMaterial(MaterialCopyPaletteComposite::new);

    public MaterialCopyPaletteComposite(Material material) {
        super(material);
    }

    @Override
    public Material composite(Material parent, boolean isClient) {
        return new DelegatingMaterial(parent) {
            @Override
            @Environment(EnvType.CLIENT)
            public MaterialRenderController getRenderController(ModuleInstance context, ItemDisplayContext mode) {
                return material.getRenderController(context, mode);
            }
        };
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }
}
