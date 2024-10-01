package smartin.miapi.forge.mixin.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ToolAction;
import net.neoforged.neoforge.common.ToolActions;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.spongepowered.asm.mixin.Mixin;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.PlatformModularItemMethods;
import smartin.miapi.item.modular.items.armor.ModularElytraItem;
import smartin.miapi.modules.abilities.toolabilities.AxeAbility;
import smartin.miapi.modules.abilities.toolabilities.HoeAbility;
import smartin.miapi.modules.abilities.toolabilities.ShovelAbility;
import smartin.miapi.modules.abilities.util.AbilityMangerProperty;
import smartin.miapi.modules.abilities.util.ItemUseAbility;
import smartin.miapi.modules.properties.armor.CanWalkOnSnow;
import smartin.miapi.modules.properties.armor.EquipmentSlotProperty;
import smartin.miapi.modules.properties.armor.IsPiglinGold;
import smartin.miapi.modules.properties.enchanment.EnchantAbilityProperty;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

import java.util.function.Predicate;

@Mixin(
        value = {
                PlatformModularItemMethods.class
        })
public interface ModularItemMixin extends IItemExtension {
    default boolean isPiglinCurrency(ItemStack stack) {
        return stack.getItem() == PiglinAi.BARTERING_ITEM;
    }

    default boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
        return IsPiglinGold.isPiglinGoldItem(stack);
    }

    default boolean canEquip(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
        return entity.getEquipmentSlotForItem(stack) == armorType || EquipmentSlotProperty.getSlot(stack).test(armorType);
    }

    default boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        if (ModularItem.isModularItem(stack)) {
            if (toolAction.equals(ToolActions.AXE_DIG)) {
                return canMine(stack, "axe");
            }
            if (toolAction.equals(ToolActions.PICKAXE_DIG)) {
                return canMine(stack, "pickaxe");
            }
            if (toolAction.equals(ToolActions.SHOVEL_DIG)) {
                return canMine(stack, "shovel");
            }
            if (toolAction.equals(ToolActions.HOE_DIG)) {
                return canMine(stack, "hoe");
            }
            if (toolAction.equals(ToolActions.SHEARS_DIG)) {
                return canMine(stack, "shear");
            }
            if (toolAction.equals(ToolActions.SWORD_DIG)) {
                return canMine(stack, "sword");
            }
            if (ToolActions.DEFAULT_AXE_ACTIONS.contains(toolAction)) {
                return hasRightClickBehaviour(stack, AxeAbility.class::isInstance);
            }
            if (ToolActions.DEFAULT_HOE_ACTIONS.contains(toolAction)) {
                return hasRightClickBehaviour(stack, HoeAbility.class::isInstance);
            }
            if (ToolActions.DEFAULT_SHOVEL_ACTIONS.contains(toolAction)) {
                return hasRightClickBehaviour(stack, ShovelAbility.class::isInstance);
            }
        }
        return false;
    }

    private static boolean canMine(ItemStack stack, String type) {
        var optional = MiningLevelProperty.property.getData(stack);
        return optional.map(stringMiningRuleMap -> stringMiningRuleMap.containsKey(type)).orElse(false);
    }

    private static boolean hasRightClickBehaviour(ItemStack stack, Predicate<? super ItemUseAbility> predicate) {
        var optional = AbilityMangerProperty.property.getData(stack);
        return optional.map(itemUseAbilityObjectMap -> itemUseAbilityObjectMap.keySet().stream().anyMatch(predicate)).orElse(false);
    }

    default int getEnchantmentValue(ItemStack stack) {
        return (int) EnchantAbilityProperty.getEnchantAbility(stack);
    }

    default boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return stack.getItem() instanceof ModularElytraItem;
    }

    default boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
        return CanWalkOnSnow.canSnowWalk(stack);
    }
}
