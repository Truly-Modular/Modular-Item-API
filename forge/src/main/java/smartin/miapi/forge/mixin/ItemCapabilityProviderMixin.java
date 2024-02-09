package smartin.miapi.forge.mixin;

/*
@Mixin(ItemCapabilityProvider.class)
public class ItemCapabilityProviderMixin {

    @Shadow
    private CapabilityItem capability;

    @Inject(
            at = @At("TAIL"),
            method = "Lyesman/epicfight/world/capabilities/provider/ItemCapabilityProvider;<init>(L;)V",
            remap = true,
            require = -1
    )
    public void miapi$injectConfiguration(ItemStack itemstack, CallbackInfo ci) {
        if (itemstack.getItem() instanceof ModularItem) {
            CapabilityItem capabilityItem = EpicFightHelper.fromJsonElement(itemstack);
            if(capabilityItem!=null){
                capability = capabilityItem;
            }
        }
    }
}
 */
