package smartin.miapi.forge;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.extensions.IForgeItem;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.abilities.toolabilities.AxeAbility;
import smartin.miapi.modules.abilities.toolabilities.HoeAbility;
import smartin.miapi.modules.abilities.toolabilities.ShovelAbility;
import smartin.miapi.modules.properties.*;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

import static smartin.miapi.events.MiapiEvents.ITEM_STACK_ATTRIBUTE_EVENT;

public interface PlatformModularItemMethods extends IForgeItem {

    @Override
    default Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
         Multimap < EntityAttribute, EntityAttributeModifier> attributeModifiers = ArrayListMultimap.create();
         ITEM_STACK_ATTRIBUTE_EVENT.invoker().adjust(new MiapiEvents.ItemStackAttributeEventHolder(stack, slot, attributeModifiers));
         return attributeModifiers;
    }

    @Override
    default boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
        return IsPiglinGold.isPiglinGoldItem(stack);
    }

    @Override
    default EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlotProperty.getSlot(stack);
    }

    @Override
    default int getMaxDamage(ItemStack stack)
    {
        return (int) DurabilityProperty.property.getValueSafe(stack);

    }

    @Override
    default boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return MiningLevelProperty.isSuitable(stack, state);
    }

    @Override
    default boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        if (stack.getItem() instanceof ModularItem) {
            if (toolAction.equals(ToolActions.AXE_DIG)) {
                return MiningLevelProperty.isSuitable(stack, "axe");
            }
            if (toolAction.equals(ToolActions.PICKAXE_DIG)) {
                return MiningLevelProperty.isSuitable(stack, "pickaxe");
            }
            if (toolAction.equals(ToolActions.SHOVEL_DIG)) {
                return MiningLevelProperty.isSuitable(stack, "shovel");
            }
            if (toolAction.equals(ToolActions.HOE_DIG)) {
                return MiningLevelProperty.isSuitable(stack, "hoe");
            }
            if (toolAction.equals(ToolActions.SHEARS_DIG)) {
                return MiningLevelProperty.isSuitable(stack, "shear");
            }
            if (toolAction.equals(ToolActions.SWORD_DIG)) {
                return MiningLevelProperty.isSuitable(stack, "sword");
            }
            if (ToolActions.DEFAULT_AXE_ACTIONS.contains(toolAction)) {
                return AbilityMangerProperty.get(stack).stream().anyMatch(AxeAbility.class::isInstance);
            }
            if (ToolActions.DEFAULT_HOE_ACTIONS.contains(toolAction)) {
                return AbilityMangerProperty.get(stack).stream().anyMatch(HoeAbility.class::isInstance);
            }
            if (ToolActions.DEFAULT_SHOVEL_ACTIONS.contains(toolAction)) {
                return AbilityMangerProperty.get(stack).stream().anyMatch(ShovelAbility.class::isInstance);
            }
        }
        return false;
    }

    @Override
    default int getEnchantmentValue(ItemStack stack)
    {
        return (int) EnchantAbilityProperty.getEnchantAbility(stack);
    }
}
