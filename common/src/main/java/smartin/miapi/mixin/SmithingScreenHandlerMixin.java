package smartin.miapi.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.item.MaterialSmithingRecipe;

/**
 * Since mojang doesnt allow Smithingrecipes to adjust the output items and miapi allows for stack smithing we need to adjust its output.
 */
@Mixin(SmithingMenu.class)
public abstract class SmithingScreenHandlerMixin {

    @Inject(
            method = "Lnet/minecraft/screen/SmithingScreenHandler;onTakeOutput(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = true,
            require = -1)
    private void miapi$adjustSmithingOutput(Player player, ItemStack stack, CallbackInfo ci) {
        if (((SmithingScreenHandlerAccessor) this).currentRecipe() instanceof MaterialSmithingRecipe) {
            Container inventory = ((ForgingScreenHandlerAccessor) this).getInput();
            inventory.setItem(1, ItemStack.EMPTY);
        }
    }
}
