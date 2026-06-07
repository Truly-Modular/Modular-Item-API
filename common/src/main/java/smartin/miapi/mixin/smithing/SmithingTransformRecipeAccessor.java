package smartin.miapi.mixin.smithing;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SmithingTransformRecipe.class)
public interface SmithingTransformRecipeAccessor {
    @Accessor("template")
    Ingredient getMiapiTemplate();

    @Accessor("base")
    Ingredient getMiapiBase();

    @Accessor("addition")
    Ingredient getMiapiAdditions();

    @Accessor("result")
    ItemStack getMiapiResult();
}
