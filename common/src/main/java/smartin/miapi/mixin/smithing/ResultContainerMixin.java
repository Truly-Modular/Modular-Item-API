package smartin.miapi.mixin.smithing;

import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.modules.properties.ItemIdProperty;

@Mixin(ResultContainer.class)
public class ResultContainerMixin {

    @ModifyVariable(
            method = "setItem"
            , at = @At("HEAD"),
            argsOnly = true)
    private ItemStack modifyStackBeforeSet(ItemStack current) {
        if (VisualModularItem.isVisualModularItem(current) && current.isDamageableItem() && current.getMaxDamage() > current.getDamageValue() + 2)
        {
            return ItemIdProperty.changeId(current);
        }
        return current;
    }
}
