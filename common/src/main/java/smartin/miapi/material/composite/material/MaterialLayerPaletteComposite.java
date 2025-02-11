package smartin.miapi.material.composite.material;

import com.google.gson.JsonElement;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.material.DelegatingMaterial;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.palette.MaskColorer;
import smartin.miapi.material.palette.MaterialRenderController;
import smartin.miapi.material.palette.SpriteColorer;
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
public class MaterialLayerPaletteComposite extends BasicOtherMaterialComposite {
    public static ResourceLocation ID = Miapi.id("material_palette_layer");
    public static MapCodec<MaterialLayerPaletteComposite> INNER_MAP_CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    StatResolver.Codecs.JSONELEMENT_CODEC.fieldOf("palette").forGetter(m -> m.mask)
            ).apply(instance, MaterialLayerPaletteComposite::new));
    public static MapCodec<MaterialLayerPaletteComposite> MAP_CODEC = CompositeFromOtherMaterial.getCodec(INNER_MAP_CODEC);
    public JsonElement mask;

    public MaterialLayerPaletteComposite(JsonElement masker) {
        super();
        this.mask = masker;
    }

    @Override
    public Material composite(Material parent, boolean isClient) {
        return new DelegatingMaterial(parent) {
            private MaterialRenderController cachedRenderController = null;

            @Override
            @Environment(EnvType.CLIENT)
            public MaterialRenderController getRenderController(ModuleInstance context, ItemDisplayContext mode) {
                if (cachedRenderController == null) {
                    MaterialRenderController parentController = parent.getRenderController(context, mode);
                    MaterialRenderController materialController = material.getRenderController(context, mode);
                    cachedRenderController = materialController;

                    if (parentController instanceof SpriteColorer basePalette &&
                        materialController instanceof SpriteColorer addPalette) {
                        try {
                            MaskColorer.Masker masker = MaskColorer.getMaskerFromJson(mask);
                            if (masker instanceof MaskColorer.SpriteMasker masker1) {
                                masker1.offsetAble = parent.hashCode() * 11 + material.hashCode()  * 47;
                            }
                            cachedRenderController = new MaskColorer(parent, basePalette, addPalette, masker);
                        } catch (RuntimeException e) {
                            Miapi.LOGGER.error("raw json was " + mask);
                            Miapi.LOGGER.error("Cannot read mask for material_palette_layer ", e);
                        }
                    } else {
                        Miapi.LOGGER.error("layer was not applied due to material controllers not supporting this");
                    }
                }
                return cachedRenderController;
            }
        };

    }

    private static int stableHash(String input) {
        int hash = 7;
        for (char c : input.toCharArray()) {
            hash = hash * 31 + c; // 31 is a prime commonly used for stable hash functions
        }
        return hash;
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }
}
