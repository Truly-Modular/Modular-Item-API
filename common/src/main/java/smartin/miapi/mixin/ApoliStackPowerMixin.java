package smartin.miapi.mixin;


import io.github.apace100.apoli.util.StackPowerUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.compat.apoli.ApoliPowersHelper;

import java.util.ArrayList;
import java.util.List;

@Mixin(StackPowerUtil.class)
public abstract class ApoliStackPowerMixin {

    @Inject(
            method = "getPowers(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;)Ljava/util/List;",
            at = @At("TAIL"),
            cancellable = true,
            remap = true,
            require = -1)
    private static void miapi$removeHook(ItemStack itemStack, EquipmentSlot slot, CallbackInfoReturnable<List<StackPowerUtil.StackPower>> cir) {
        if (itemStack.getItem() instanceof ModularItem) {
            List<StackPowerUtil.StackPower> powers = cir.getReturnValue();
            if (powers == null) {
                powers = new ArrayList<>();
            }
            cir.setReturnValue(ApoliPowersHelper.getPowers(itemStack, slot, powers));
        }
    }
}
