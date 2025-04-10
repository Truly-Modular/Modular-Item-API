package smartin.miapi.forge.mixin.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.spongepowered.asm.mixin.Mixin;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.items.ExampleModularItem;
import smartin.miapi.item.modular.items.ExampleModularStrackableItem;
import smartin.miapi.item.modular.items.armor.*;
import smartin.miapi.item.modular.items.bows.ModularArrow;
import smartin.miapi.item.modular.items.bows.ModularBow;
import smartin.miapi.item.modular.items.bows.ModularCrossbow;
import smartin.miapi.item.modular.items.tools.*;
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
                ExampleModularItem.class,
                ExampleModularStrackableItem.class,

                ModularArrow.class,
                ModularCrossbow.class,
                ModularBow.class,

                ModularAxe.class,
                ModularHoe.class,
                ModularPickaxe.class,
                ModularShovel.class,
                ModularSword.class,
                ModularWeapon.class,

                ModularHelmet.class,
                ModularChestPlate.class,
                ModularElytraItem.class,
                ModularLeggings.class,
                ModularBoots.class
        })
public abstract class ModularItemTestMixin implements IItemExtension {
    public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
        return IsPiglinGold.isPiglinGoldItem(stack);
    }

    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
        return entity.getEquipmentSlotForItem(stack) == armorType || EquipmentSlotProperty.getSlot(stack).test(armorType);
    }

    public boolean canPerformAction(ItemStack stack, ItemAbility toolAction) {
        Miapi.LOGGER.info("can perform action check");
        if (ModularItem.isModularItem(stack)) {
            if (toolAction.equals(ItemAbilities.AXE_DIG)) {
                return canMine(stack, "axe");
            }
            if (toolAction.equals(ItemAbilities.PICKAXE_DIG)) {
                return canMine(stack, "pickaxe");
            }
            if (toolAction.equals(ItemAbilities.SHOVEL_DIG)) {
                return canMine(stack, "shovel");
            }
            if (toolAction.equals(ItemAbilities.HOE_DIG)) {
                return canMine(stack, "hoe");
            }
            if (toolAction.equals(ItemAbilities.SHEARS_DIG)) {
                return canMine(stack, "shear");
            }
            if (toolAction.equals(ItemAbilities.SWORD_DIG)) {
                return canMine(stack, "sword");
            }
            if (ItemAbilities.DEFAULT_AXE_ACTIONS.contains(toolAction)) {
                return hasRightClickBehaviour(stack, AxeAbility.class::isInstance);
            }
            if (ItemAbilities.DEFAULT_HOE_ACTIONS.contains(toolAction)) {
                return hasRightClickBehaviour(stack, HoeAbility.class::isInstance);
            }
            if (ItemAbilities.DEFAULT_SHOVEL_ACTIONS.contains(toolAction)) {
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

    public int getEnchantmentValue(ItemStack stack) {
        return (int) EnchantAbilityProperty.getEnchantAbility(stack);
    }

    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return stack.getItem() instanceof ModularElytraItem;
    }

    public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
        return CanWalkOnSnow.canSnowWalk(stack);
    }
}
