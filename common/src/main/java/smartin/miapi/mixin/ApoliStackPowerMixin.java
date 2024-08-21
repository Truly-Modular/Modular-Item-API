package smartin.miapi.mixin;


import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.util.StackPowerUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.item.modular.ModularItem;

import java.util.ArrayList;
import java.util.List;

@Mixin(StackPowerUtil.class)
public abstract class ApoliStackPowerMixin {

    @ModifyReturnValue(
            method = "getPowers(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;)Ljava/util/List;",
            at = @At("RETURN"),
            remap = true,
            require = -1)
    private static List<StackPowerUtil.StackPower> miapi$removeHook(List<StackPowerUtil.StackPower> old,ItemStack itemStack, EquipmentSlot slot) {
        if (itemStack.getItem() instanceof ModularItem) {
            List<StackPowerUtil.StackPower> powers = old;
            if (powers == null) {
                powers = new ArrayList<>();
            }
            //return ApoliPowersHelper.getPowers(itemStack, slot, powers);
        }
        return old;
    }
}
