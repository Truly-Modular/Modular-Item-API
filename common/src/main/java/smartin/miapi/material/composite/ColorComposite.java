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

import java.util.Objects;

/**
 * This Composite assigns a specific color to a material.
 *
 * @header Color Composite
 * @description_start
 * The Color Composite modifies the visual appearance of a material by applying a fixed color value.
 * This allows customization of material colors for better distinction and aesthetics.
 * @description_end
 * @path /data_types/composites/color
 * @data color: A string representing the color value, which is converted into an ARGB integer.
 */
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ColorComposite that = (ColorComposite) obj;
        return Objects.equals(color, that.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color);
    }

}
