package smartin.miapi.mixin;

import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.modules.properties.ItemIdProperty;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {

    @Inject(
            method = "createResult()V",
            at = @At("TAIL")
    )
    public void miapi$preventFullBreak(CallbackInfo ci) {
        ItemStack current =  (
                (ForgingScreenHandlerAccessor) this)
                .getResultSlots()
                .getItem(0);
        if (VisualModularItem.isModularItem(current) && current.isDamageableItem() && !MiapiConfig.INSTANCE.server.other.fullBreakModularItems) {
            ((ForgingScreenHandlerAccessor) this)
                    .getResultSlots()
                    .setItem(
                            0,
                            ItemIdProperty.changeId(current));
        }
    }
}
