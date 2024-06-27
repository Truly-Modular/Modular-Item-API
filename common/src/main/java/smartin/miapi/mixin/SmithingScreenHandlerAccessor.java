package smartin.miapi.mixin;

import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.crafting.SmithingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SmithingMenu.class)
public interface SmithingScreenHandlerAccessor {

    @Accessor("currentRecipe")
    SmithingRecipe currentRecipe();

}
