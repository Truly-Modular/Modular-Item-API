package smartin.miapi.material.composite;

import com.google.gson.JsonElement;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.material.DefaultMaterial;
import smartin.miapi.material.DelegatingMaterial;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.palette.FallbackColorer;
import smartin.miapi.material.palette.MaterialRenderController;
import smartin.miapi.material.palette.MaterialRenderControllers;
import smartin.miapi.modules.ModuleInstance;

import java.util.Objects;

/**
 * This Composite controls the material's normal visuals using a palette system.
 *
 * @header Palette Composite
 * @description_start
 * The Palette Composite modifies the visual appearance of a material by applying a color palette.
 * It allows dynamic texture and color adjustments based on the provided palette data, influencing
 * how the material is rendered in the game.
 * @description_end
 * @path /data_types/composites/palette
 * @data palette: A JSON element defining the color palette and rendering rules.
 */
public record PaletteComposite(JsonElement json) implements Composite {
    public static ResourceLocation ID = Miapi.id("material_palette");
    public static MapCodec<Composite> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    StatResolver.Codecs.JSONELEMENT_CODEC.fieldOf("palette").forGetter((composite -> {
                        if (composite instanceof PaletteComposite materialCopyComposite) {
                            return materialCopyComposite.json();
                        }
                        return null;
                    }))
            ).apply(instance, (element -> {
                if (smartin.miapi.Environment.isClient()) {
                    try {
                        MaterialRenderControllers.creators.get(
                                element.getAsJsonObject().get("type").getAsString()).createPalette(element, new DefaultMaterial());
                    } catch (RuntimeException e) {
                        Miapi.LOGGER.error("could not decode palette in Palette-Composite", e);
                    }
                }
                return new PaletteComposite(element);
            })));

    @Override
    public Material composite(Material parent, boolean isClient) {
        return new DelegatingMaterial(parent) {
            @Environment(EnvType.CLIENT)
            final MaterialRenderController controller =
                    getController(json, parent);

            @Environment(EnvType.CLIENT)
            public MaterialRenderController getRenderController(ModuleInstance context, ItemDisplayContext mode) {
                return controller;
            }

            @Environment(EnvType.CLIENT)
            public int getColor(ModuleInstance context){
                return controller.getAverageColor().abgr();
            }

            @Environment(EnvType.CLIENT)
            public int getColor(ModuleInstance context, ItemDisplayContext mode){
                return controller.getAverageColor().abgr();
            }
        };
    }

    @Environment(EnvType.CLIENT)
    private MaterialRenderController getController(JsonElement jsonElement, Material parent) {
        try {
            return MaterialRenderControllers.creators.get(
                    jsonElement.getAsJsonObject().get("type").getAsString()).createPalette(jsonElement, parent);
        } catch (RuntimeException e) {
            return new FallbackColorer(parent);
        }
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PaletteComposite that = (PaletteComposite) obj;
        return Objects.equals(json, that.json);
    }

    @Override
    public int hashCode() {
        return Objects.hash(json);
    }

}
