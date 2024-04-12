package smartin.miapi.mixin;

import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import smartin.miapi.modules.properties.ItemIdProperty;

@Mixin(CraftingResultInventory.class)
public class AnvilScreenHandlerMixin {
    @ModifyVariable(method = "setStack(ILnet/minecraft/item/ItemStack;)V", at = @At(value = "HEAD"), ordinal = 0)
    private ItemStack miapi$damageEventSource(ItemStack original) {
        return ItemIdProperty.changeId(original);
    }
}
