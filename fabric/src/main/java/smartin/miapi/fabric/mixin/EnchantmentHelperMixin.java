package smartin.miapi.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.item.FakeEnchantment;
import smartin.miapi.item.modular.ModularItem;

@Mixin(value = EnchantmentHelper.class, priority = 700)
public class EnchantmentHelperMixin {

    @ModifyReturnValue(
            method = "getLevel(Lnet/minecraft/enchantment/Enchantment;Lnet/minecraft/item/ItemStack;)I",
            at = @At("RETURN")
    )
    private static int miapi$modifyPossibleEntries(int original, Enchantment enchantment, ItemStack stack) {
        if (stack.getItem() instanceof ModularItem) {
            return FakeEnchantment.getFakeLevel(enchantment, stack, original);
        }
        return original;
    }

    @Inject(
            method = "forEachEnchantment(Lnet/minecraft/enchantment/EnchantmentHelper$Consumer;Lnet/minecraft/item/ItemStack;)V",
            at = @At("TAIL"),
            cancellable = true)
    private static void miapi$addFakeEnchants(EnchantmentHelper.Consumer consumer, ItemStack stack, CallbackInfo ci) {
        FakeEnchantment.addEnchantments(consumer, stack);
    }
}
