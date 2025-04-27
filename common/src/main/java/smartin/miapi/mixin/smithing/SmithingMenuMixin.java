package smartin.miapi.mixin.smithing;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.item.MaterialSmithingRecipe;
import smartin.miapi.item.modular.VisualModularItem;

@Mixin(SmithingMenu.class)
public class SmithingMenuMixin {

    @Inject(method = "onTake(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"))
    private void miapi$clearSmithingStart(Player player, ItemStack stack, CallbackInfo ci) {
        if (VisualModularItem.isVisualModularItem(stack)) {
            SmithingMenu menu = (SmithingMenu) (Object) this;
            var smithing = ((SmithingScreenHandlerAccessor) menu).currentRecipe();
            if(smithing.value() instanceof MaterialSmithingRecipe){
                menu.getSlot(1).getItem().setCount(0);
                menu.getSlot(1).set(ItemStack.EMPTY);
            }
        }
    }
}
