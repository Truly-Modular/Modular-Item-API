package smartin.miapi.mixin;


import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.util.StackPowerUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.compat.apoli.ApoliPowersHelper;

import java.util.ArrayList;
import java.util.List;

@Mixin(StackPowerUtil.class)
public abstract class ApoliStackPowerMixin {

    @ModifyReturnValue(
            method = "getPowers(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EquipmentSlot;)Ljava/util/List;",
            at = @At("RETURN"),
            remap = true,
            require = -1)
    private static List<StackPowerUtil.StackPower> miapi$removeHook(List<StackPowerUtil.StackPower> original, ItemStack itemStack, EquipmentSlot slot) {
        if (itemStack.getItem() instanceof ModularItem) {
            List<StackPowerUtil.StackPower> powers = original;
            if (powers == null) {
                powers = new ArrayList<>();
            }
            return ApoliPowersHelper.getPowers(itemStack, slot, powers);
        }
        return original;
    }
}
