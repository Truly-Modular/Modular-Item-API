package smartin.miapi.mixin;

import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.modules.properties.ItemIdProperty;

@Mixin(ResultContainer.class)
public class CraftingResultInventorMixin {
    @ModifyVariable(method = "setItem", at = @At(value = "HEAD"), ordinal = 0)
    private ItemStack miapi$adjustItemIdOnAnvilRepair(ItemStack original) {
        if (original != null && original.getItem() instanceof VisualModularItem) {
            return ItemIdProperty.changeId(original);
        }
        return original;
    }
}
