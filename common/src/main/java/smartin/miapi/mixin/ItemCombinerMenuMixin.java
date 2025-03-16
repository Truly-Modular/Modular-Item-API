package smartin.miapi.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.item.MaterialSmithingRecipe;

@Mixin(ItemCombinerMenu.class)
public class ItemCombinerMenuMixin {


    @Inject(
            method = "quickMoveStack(Lnet/minecraft/world/entity/player/Player;I)Lnet/minecraft/world/item/ItemStack;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/Slot;getItem()Lnet/minecraft/world/item/ItemStack;",
                    shift = At.Shift.AFTER,
                    by = +1
            ))
    private void miapi$clearSmithingStart(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if(index==3){
            ItemCombinerMenu itemCombinerMenu = (ItemCombinerMenu) (Object) this;
            if(itemCombinerMenu instanceof SmithingMenu menu){
                var recipe = ((SmithingScreenHandlerAccessor) menu).currentRecipe();
                if(recipe.value() instanceof MaterialSmithingRecipe){
                    menu.getSlot(1).getItem().setCount(0);
                    menu.getSlot(1).set(ItemStack.EMPTY);
                }
            }
        }
    }
}
