package smartin.miapi.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.item.modular.VisualModularItem;

@Mixin(SmithingMenu.class)
public class SmithingMenuMixin {

    @Inject(method = "onTake(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"))
    private void miapi$playerTickStart(Player player, ItemStack stack, CallbackInfo ci) {
        if (VisualModularItem.isModularItem(stack)) {
            SmithingMenu menu = (SmithingMenu) (Object) this;
            menu.getSlot(1).getItem().setCount(0);
        }
    }
}
