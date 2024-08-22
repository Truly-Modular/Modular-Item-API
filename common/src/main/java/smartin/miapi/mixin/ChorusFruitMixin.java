package smartin.miapi.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ChorusFruitItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import smartin.miapi.registries.RegistryInventory;

@Mixin(ChorusFruitItem.class)
public class ChorusFruitMixin {

    @ModifyReturnValue(
            method = "finishUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;",
            at = @At("RETURN"))
    private ItemStack miapi$teleportBlockEffect(ItemStack original, ItemStack stack, World world, LivingEntity user) {
        if (user.hasStatusEffect(RegistryInventory.teleportBlockEffect)) {
            return stack;
        }
        return original;
    }
}
