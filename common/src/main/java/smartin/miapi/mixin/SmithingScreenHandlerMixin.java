package smartin.miapi.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SmithingScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.item.NetheriteSmithingRecipe;

@Mixin(SmithingScreenHandler.class)
public abstract class SmithingScreenHandlerMixin {

    @Inject(
            method = "Lnet/minecraft/screen/SmithingScreenHandler;onTakeOutput(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = true,
            require = -1)
    private void miapi$adjustSmithingOutput(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if(((SmithingScreenHandlerAccessor)this).currentRecipe() instanceof NetheriteSmithingRecipe){
            Inventory inventory = ((ForgingScreenHandlerAccessor)this).getInput();
            inventory.setStack(1,ItemStack.EMPTY);
        }
    }
}
