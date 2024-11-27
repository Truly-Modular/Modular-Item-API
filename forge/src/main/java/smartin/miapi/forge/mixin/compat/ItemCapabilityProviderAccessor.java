package smartin.miapi.forge.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.provider.ItemCapabilityProvider;

@Mixin(ItemCapabilityProvider.class)
public interface ItemCapabilityProviderAccessor {
    @Accessor(
            value = "capability",
            remap = false)
    void setCapability(CapabilityItem capability);
}
