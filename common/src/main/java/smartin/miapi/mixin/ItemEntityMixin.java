package smartin.miapi.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.FireProof;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @Inject(method = "isFireImmune()Z", at = @At(value = "HEAD"), cancellable = true)
    private void miapi$isFireImmuneOverride(CallbackInfoReturnable<Boolean> cir) {
        ItemEntity entity = (ItemEntity) (Object) (this);
        ItemStack stack = entity.getStack();
        if (stack.getItem() instanceof ModularItem) {
            cir.setReturnValue(FireProof.fireProof(stack));
        }
    }
}
