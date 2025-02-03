package smartin.miapi.material.composite;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.material.DelegatingMaterial;
import smartin.miapi.material.base.Material;

public record DyeableComposite(boolean dyeAble) implements Composite {
    public static ResourceLocation ID = Miapi.id("dye_able");
    public static MapCodec<Composite> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    Miapi.FIXED_BOOL_CODEC.fieldOf("is_dye_able").forGetter((composite -> {
                        if (composite instanceof DyeableComposite materialCopyComposite) {
                            return materialCopyComposite.dyeAble();
                        }
                        return null;
                    }))
            ).apply(instance, DyeableComposite::new));

    @Override
    public Material composite(Material parent, boolean isClient) {
        return new DelegatingMaterial(parent) {
            @Override
            public boolean canBeDyed() {
                return dyeAble();
            }
        };
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }
}
