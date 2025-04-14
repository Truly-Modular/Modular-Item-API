package smartin.miapi.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.enchanment.AllowedEnchantments;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {

    @ModifyReturnValue(method = "isPrimaryItem(Lnet/minecraft/world/item/ItemStack;)Z", at = @At(value = "RETURN"))
    private boolean miapi$adjustPrimaryItem(boolean original, ItemStack itemStack) {
        if (ModularItem.isModularItem(itemStack)) {
            Enchantment enchantment = (Enchantment) (Object) (this);
            //return AllowedEnchantments.isPrimaryAllowed(itemStack, enchantment, original);
        }
        return original;
    }

    @ModifyReturnValue(method = "isSupportedItem", at = @At(value = "RETURN"))
    private boolean miapi$adjustSupportedItem(boolean original, ItemStack itemStack) {
        if (ModularItem.isModularItem(itemStack)) {
            Enchantment enchantment = (Enchantment) (Object) (this);
            ModuleInstance moduleInstance = ItemModule.getModules(itemStack);
            if (moduleInstance != null && moduleInstance.registryAccess != null) {
                Holder<Enchantment> holder = moduleInstance.registryAccess.registry(Registries.ENCHANTMENT).get().wrapAsHolder(enchantment);
                if(holder!=null){
                    return AllowedEnchantments.isAllowed(itemStack, holder, original);
                }
            }
        }
        return original;
    }

    @ModifyReturnValue(method = "canEnchant(Lnet/minecraft/world/item/ItemStack;)Z", at = @At(value = "RETURN"))
    private boolean miapi$adjustcanEnchant(boolean original, ItemStack itemStack) {
        if (ModularItem.isModularItem(itemStack)) {
            Enchantment enchantment = (Enchantment) (Object) (this);
            ModuleInstance moduleInstance = ItemModule.getModules(itemStack);
            if (moduleInstance != null && moduleInstance.registryAccess != null) {
                Holder<Enchantment> holder = moduleInstance.registryAccess.registry(Registries.ENCHANTMENT).get().wrapAsHolder(enchantment);
                if(holder!=null){
                    return AllowedEnchantments.isAllowed(itemStack, holder, original);
                }
            }
        }
        return original;
    }
}
