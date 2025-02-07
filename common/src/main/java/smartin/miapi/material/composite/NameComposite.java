package smartin.miapi.material.composite;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.material.DelegatingMaterial;
import smartin.miapi.material.base.Material;

/**
 * This Composite allows a material to have a custom name.
 *
 * @header Name Composite
 * @description_start
 * The Name Composite overrides the default name of a material with a specified custom name.
 * This enables materials to have unique or dynamic names, which can be displayed in UI elements.
 * @description_end
 * @path /data_types/composites/name
 * @data name: The custom name assigned to the material.
 */

public record NameComposite(Component name) implements Composite {
    public static ResourceLocation ID = Miapi.id("material_name");
    public static MapCodec<Composite> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    ComponentSerialization.CODEC.fieldOf("name").forGetter((composite -> {
                        if (composite instanceof NameComposite materialCopyComposite) {
                            return materialCopyComposite.name();
                        }
                        return null;
                    }))
            ).apply(instance, NameComposite::new));

    @Override
    public Material composite(Material parent, boolean isClient) {
        return new DelegatingMaterial(parent) {
            public Component getTranslation() {
                return name();
            }
        };
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }
}
