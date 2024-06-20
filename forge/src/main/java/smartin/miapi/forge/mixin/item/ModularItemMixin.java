package smartin.miapi.forge.mixin.item;

import com.google.common.collect.Multimap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import org.spongepowered.asm.mixin.Mixin;
import smartin.miapi.forge.compat.ModularItemInject;
import smartin.miapi.item.modular.items.*;

@Mixin(
        value = {
                ExampleModularItem.class,
                ExampleModularStrackableItem.class,
                ModularArrow.class,
                ModularAxe.class,
                ModularBoomerangItem.class,
                ModularBoots.class,
                ModularBow.class,
                ModularChestPlate.class,
                ModularCrossbow.class,
                ModularElytraItem.class,
                ModularHelmet.class,
                ModularHoe.class,
                ModularLeggings.class,
                ModularPickaxe.class,
                ModularShovel.class,
                ModularSword.class,
                ModularWeapon.class
        })
public abstract class ModularItemMixin implements ModularItemInject {

    public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
        return makesPiglinsNeutralModular(stack, wearer);
    }


    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return getEquipmentSlotModular(stack);
    }


    public int getMaxDamage(ItemStack stack) {
        return getMaxDamageModular(stack);

    }

    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return isCorrectToolForDropsModular(stack, state);
    }


    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return canPerformActionModular(stack, toolAction);
    }


    public int getEnchantmentValue(ItemStack stack) {
        return getEnchantmentValueModular(stack);
    }

    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return getAttributeModifiersModular(slot, stack);
    }
}
