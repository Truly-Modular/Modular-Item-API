package smartin.miapi.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.items.bows.ModularCrossbow;

@Mixin(CrossbowItem.class)
public class CrossBowMixin {

    @ModifyReturnValue(method = "Lnet/minecraft/world/item/CrossbowItem;getChargeDuration(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)I", at = @At("RETURN"))
    private static int miapi$adjustIsItem(int original, ItemStack stack, LivingEntity shooter) {
        if (ModularItem.isModularItem(stack)) {
            return ModularCrossbow.getChargeDuration(stack, shooter);
        }
        return original;
    }
}
