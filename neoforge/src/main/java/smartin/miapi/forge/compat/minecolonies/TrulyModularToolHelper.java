package smartin.miapi.forge.compat.minecolonies;

import com.minecolonies.api.compatibility.tinkers.TinkersToolProxy;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.base.Material;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.abilities.toolabilities.AxeAbility;
import smartin.miapi.modules.abilities.toolabilities.HoeAbility;
import smartin.miapi.modules.abilities.toolabilities.ShovelAbility;
import smartin.miapi.modules.properties.armor.EquipmentSlotProperty;
import smartin.miapi.modules.properties.attributes.AttributeUtil;

import static smartin.miapi.forge.Injection.NeoForgeItemMethods.canMine;
import static smartin.miapi.forge.Injection.NeoForgeItemMethods.hasRightClickBehaviour;

/**
 * yeah, we are pretending to be tinkers for compat
 * problem?
 */
public class TrulyModularToolHelper extends TinkersToolProxy {
    TinkersToolProxy delegate;

    public TrulyModularToolHelper(TinkersToolProxy delegate) {
        this.delegate = delegate;
    }

    /**
     * Check if a certain itemstack is a tinkers weapon.
     *
     * @param stack the stack to check for.
     * @return true if so.
     */
    @Override
    public boolean isTinkersWeapon(@NotNull final ItemStack stack) {
        if (ModularItem.isModularItem(stack)) {
            return true;
        }
        return delegate.isTinkersWeapon(stack);
    }

    /**
     * Check if a certain item stack is a tinkers tool of the given tool type.
     *
     * @param stack    the stack to check for.
     * @param toolType the tool type.
     * @return true if so.
     */
    @Override
    public boolean isTinkersTool(@Nullable final ItemStack stack, final EquipmentTypeEntry toolType) {
        if (stack == null || !ModularItem.isModularItem(stack)) {
            return delegate.isTinkersTool(stack, toolType);
        }

        // Mining tools
        if (ModEquipmentTypes.axe.get().equals(toolType)) {
            return canMine(stack, "axe") || hasRightClickBehaviour(stack, AxeAbility.class::isInstance);
        }

        if (ModEquipmentTypes.pickaxe.get().equals(toolType)) {
            return canMine(stack, "pickaxe");
        }

        if (ModEquipmentTypes.shovel.get().equals(toolType)) {
            return canMine(stack, "shovel") || hasRightClickBehaviour(stack, ShovelAbility.class::isInstance);
        }

        if (ModEquipmentTypes.hoe.get().equals(toolType)) {
            return canMine(stack, "hoe") || hasRightClickBehaviour(stack, HoeAbility.class::isInstance);
        }

        if (ModEquipmentTypes.shears.get().equals(toolType)) {
            return canMine(stack, "shear");
        }

        if (ModEquipmentTypes.sword.get().equals(toolType)) {
            return canMine(stack, "sword") || stack.getItem() instanceof SwordItem;
        }

        // Armor
        if (ModEquipmentTypes.boots.get().equals(toolType)) {
            return isArmorForSlot(stack, EquipmentSlot.FEET);
        }

        if (ModEquipmentTypes.leggings.get().equals(toolType)) {
            return isArmorForSlot(stack, EquipmentSlot.LEGS);
        }

        if (ModEquipmentTypes.chestplate.get().equals(toolType)) {
            return isArmorForSlot(stack, EquipmentSlot.CHEST);
        }

        if (ModEquipmentTypes.helmet.get().equals(toolType)) {
            return isArmorForSlot(stack, EquipmentSlot.HEAD);
        }

        return delegate.isTinkersTool(stack, toolType);
    }

    private boolean isArmorForSlot(ItemStack stack, EquipmentSlot slot) {
        EquipmentSlotGroup group = EquipmentSlotProperty.getSlot(stack);
        return group != null && group.test(slot);
    }

    /**
     * Calculate the actual attack damage of the tinkers weapon.
     *
     * @param stack the stack.
     * @return the attack damage.
     */
    @Override
    public double getAttackDamage(@NotNull final ItemStack stack) {
        if (!ModularItem.isModularItem(stack)) {
            return AttributeUtil.getActualValue(stack, EquipmentSlot.MAINHAND, Attributes.ATTACK_DAMAGE.value());
        }
        return delegate.getAttackDamage(stack);
    }

    /**
     * Calculate the tool level of the stack.
     *
     * @param stack the stack.
     * @return the tool level
     */
    @Override
    public int getToolLevel(@NotNull final ItemStack stack) {
        if (!ModularItem.isModularItem(stack)) {
            int detected = -1;
            for (ModuleInstance m : ItemModule.getModules(stack).allSubModules()) {
                Material material = MaterialProperty.getMaterial(m);
                if (material != null) {
                    detected = (int) Math.floor(Math.max(detected, material.getDouble("hardness")));
                }
            }
            return detected;
        }
        return delegate.getToolLevel(stack);
    }

    /**
     * Checks to see if STACK is a tinker's tool, and if it is, it checks it's NBT tags to see if it's broken.
     *
     * @param stack the item in question.
     * @return boolean whether the stack is broken or not.
     */
    public boolean checkTinkersBroken(@Nullable final ItemStack stack) {
        if (stack != null && ModularItem.isModularItem(stack)) {
            return false;
        }
        return delegate.checkTinkersBroken(stack);
    }
}
