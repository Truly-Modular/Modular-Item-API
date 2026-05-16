package smartin.miapi.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.config.MiapiConfig;
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
                if (!inventoryScreen.layoutManager.isMouseOver((int) mouseX, (int) mouseY)) {
                    return null;
                }
            }
        }
        return original;
    }

    @WrapMethod(
            method = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V")
    private void miapi$inventoryItemBatchRendering(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, Operation<Void> original) {
        if (MiapiConfig.getClientConfig().render.batch.enableInventory) {
            MiapiItemModel.startItemBatch();
        }
        original.call(guiGraphics, mouseX, mouseY, partialTick);
        if (MiapiConfig.getClientConfig().render.batch.enableInventory) {
            MiapiItemModel.finishBatch(guiGraphics.bufferSource());
        }
    }
}
