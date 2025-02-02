package smartin.miapi.material.composite;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.MaterialProperty;

public record MaterialOverwriteComposite(Material material) implements Composite {
    public static ResourceLocation ID = Miapi.id("material_replace");
    public static MapCodec<Composite> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    Miapi.ID_CODEC.fieldOf("base").forGetter((composite -> {
                        if(composite instanceof MaterialOverwriteComposite materialOverwriteComposite){
                            return materialOverwriteComposite.material().getID();
                        }
                        return null;
                    }))
            ).apply(instance, MaterialOverwriteComposite::fromID));
    @Override
    public Material composite(Material parent, boolean isClient) {
        return this.material();
    }

    public static MaterialOverwriteComposite fromID(ResourceLocation id) {
        return new MaterialOverwriteComposite(MaterialProperty.materials.get(id));
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }
}
