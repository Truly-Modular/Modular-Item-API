package smartin.miapi.material.composite;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.material.DelegatingMaterial;
import smartin.miapi.material.base.Material;

import java.util.Objects;
import java.util.Optional;

/**
 * This Composite allows any ingredient to be treated as a valid material component.
 *
 * @header Any Ingredient Composite
 * @description_start
 * The Any Ingredient Composite ensures that any item can contribute to material composition.
 * It assigns a default value and priority to all ingredients, making them universally valid
 * in crafting or material processing.
 * @description_end
 * @path /data_types/composites/any_ingredient
 * @data base: An optional base material reference (not used in this implementation).
 */
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

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AnyIngredientComposite;
    }

    @Override
    public int hashCode() {
        return Objects.hash(AnyIngredientComposite.class);
    }

}
