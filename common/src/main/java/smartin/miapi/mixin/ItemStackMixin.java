package smartin.miapi.mixin;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.FakeItemTagProperty;

@Mixin(value = ItemStack.class, priority = 2000)
abstract class ItemStackMixin {

    @Inject(method = "is(Lnet/minecraft/tags/TagKey;)Z", at = @At("TAIL"), cancellable = true)
    public void miapi$injectItemTag(TagKey<Item> tag, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof ModularItem) {
            if (!cir.getReturnValue()) {
                cir.setReturnValue(FakeItemTagProperty.hasTag(tag.location(), stack));
            }
        }
    }
}
