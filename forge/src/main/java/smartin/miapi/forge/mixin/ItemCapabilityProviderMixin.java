package smartin.miapi.forge.mixin;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.forge.compat.epic_fight.EpicFightCompatProperty;
import smartin.miapi.item.modular.ModularItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.provider.ItemCapabilityProvider;

@Mixin(ItemCapabilityProvider.class)
public class ItemCapabilityProviderMixin {
    @Shadow
    private CapabilityItem capability;

    @Inject(
            at = @At("TAIL"),
            method = "<init>",
            remap = true,
            require = -1
    )
    public void miapi$injectConfiguration(ItemStack itemstack, CallbackInfo ci) {
        if (itemstack.getItem() instanceof ModularItem) {
            CapabilityItem capabilityItem = EpicFightCompatProperty.get(itemstack);
            if (capabilityItem != null) capability = capabilityItem;
        }
    }
}
