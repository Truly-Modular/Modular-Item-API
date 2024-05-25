package smartin.miapi.forge.mixin.item;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import org.spongepowered.asm.mixin.Mixin;
import smartin.miapi.forge.compat.ModularItemInject;
import smartin.miapi.item.modular.items.ModularBoots;

@Mixin(value = ModularBoots.class)
public abstract class ModularBootsMixin implements ModularItemInject {
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return canPerformActionModular(stack, toolAction);
    }
}