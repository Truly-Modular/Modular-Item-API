package smartin.miapi.mixin;

import net.minecraft.world.inventory.SmithingMenu;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Since mojang doesnt allow Smithingrecipes to adjust the output items and miapi allows for stack smithing we need to adjust its output.
 */
@Mixin(SmithingMenu.class)
public abstract class SmithingScreenHandlerMixin {
    //TODO:figure out smithing recipes again
}
