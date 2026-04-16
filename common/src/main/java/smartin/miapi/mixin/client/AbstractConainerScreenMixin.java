package smartin.miapi.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import smartin.miapi.modules.properties.inventory.screen.DefaultInventoryScreenHandler;
import smartin.miapi.modules.properties.inventory.screen.InventoryScreen;

@Mixin(AbstractContainerScreen.class)
public class AbstractConainerScreenMixin {


    @ModifyReturnValue(
            method = "findSlot",
            at = @At("RETURN"))
    private Slot miapi$insertSprites(Slot original, double mouseX, double mouseY) {
        AbstractContainerScreen screen = (AbstractContainerScreen) (Object) this;
        if (screen instanceof InventoryScreen inventoryScreen) {
            if (original instanceof DefaultInventoryScreenHandler.ManagedSlot) {
                if (!inventoryScreen.layoutManager.isMouseOver((int)mouseX, (int)mouseY)) {
                    return null;
                }
            }
        }
        return original;
    }
}
