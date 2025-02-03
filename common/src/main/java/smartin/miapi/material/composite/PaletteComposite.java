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
}
