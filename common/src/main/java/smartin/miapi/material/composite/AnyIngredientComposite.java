package smartin.miapi.material.composite;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.material.DelegatingMaterial;
import smartin.miapi.material.base.Material;

import java.util.Optional;

public record AnyIngredientComposite() implements Composite {
    public static ResourceLocation ID = Miapi.id("any_ingredient");
    public static MapCodec<Composite> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    Miapi.ID_CODEC.optionalFieldOf("base").forGetter(c -> Optional.empty())
            ).apply(instance, AnyIngredientComposite::fromID));

    @Override
    public Material composite(Material parent, boolean isClient) {
        return new DelegatingMaterial(parent) {
            public Double getPriorityOfIngredientItem(ItemStack ingredient) {
                return 1.0;
            }
            public double getValueOfItem(ItemStack ingredient) {
                return 1.0;
            }
        };
    }

    public static AnyIngredientComposite fromID(Optional<ResourceLocation> resourceLocation) {
        return new AnyIngredientComposite();
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }
}
