package smartin.miapi.forge.mixin.item;

import com.google.common.collect.Multimap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import org.spongepowered.asm.mixin.Mixin;
import smartin.miapi.forge.compat.ModularItemInject;
import smartin.miapi.item.modular.items.ModularCrossbow;

@Mixin(value = ModularCrossbow.class)
public abstract class ModularCrossbowMixin implements ModularItemInject {
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return canPerformActionModular(stack, toolAction);
    }

    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return isCorrectToolForDropsModular(stack, state);
    }

    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return getModularAttributeModifiers(slot, stack);
    }
}
