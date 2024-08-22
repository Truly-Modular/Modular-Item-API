package smartin.miapi.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.modules.properties.FireProof;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @ModifyReturnValue(method = "isFireImmune()Z", at = @At(value = "RETURN"))
    private boolean miapi$isFireImmuneOverride(boolean original) {
        ItemEntity entity = (ItemEntity) (Object) (this);
        ItemStack stack = entity.getStack();
        if (stack.getItem() instanceof VisualModularItem) {
            return FireProof.fireProof(stack);
        }
        return original;
    }
}
