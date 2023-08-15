package smartin.miapi.mixin;

import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.screen.SmithingScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SmithingScreenHandler.class)
public interface SmithingScreenHandlerAccessor {

    @Accessor("currentRecipe")
    SmithingRecipe currentRecipe();

}
