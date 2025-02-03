package smartin.miapi.material.composite;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redpxnda.nucleus.util.Color;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import smartin.miapi.Miapi;
import smartin.miapi.craft.stat.StatProvidersMap;
import smartin.miapi.material.DelegatingMaterial;
import smartin.miapi.material.base.Material;
import smartin.miapi.modules.ModuleInstance;

public record ColorComposite(String color) implements Composite {
    public static ResourceLocation ID = Miapi.id("material_color");
    public static MapCodec<Composite> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    StatProvidersMap.Codec.STRING.fieldOf("color").forGetter((composite -> {
                        if (composite instanceof ColorComposite materialCopyComposite) {
                            return materialCopyComposite.color();
                        }
                        return null;
                    }))
            ).apply(instance, ColorComposite::new));

    @Override
    public Material composite(Material parent, boolean isClient) {
        int colorInt = new Color(color).argb();
        return new DelegatingMaterial(parent) {
            public int getColor(ModuleInstance context){
                return colorInt;
            }
            public int getColor(ModuleInstance context, ItemDisplayContext mode){
                return colorInt;
            }
        };
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }
}
