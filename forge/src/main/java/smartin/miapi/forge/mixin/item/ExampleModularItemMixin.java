package smartin.miapi.forge.mixin.item;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import org.spongepowered.asm.mixin.Mixin;
import smartin.miapi.forge.compat.ModularItemInject;

@Mixin(value = smartin.miapi.item.modular.items.ExampleModularItem.class)
public abstract class ExampleModularItemMixin implements ModularItemInject {
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return canPerformActionModular(stack,toolAction);
    }
}
