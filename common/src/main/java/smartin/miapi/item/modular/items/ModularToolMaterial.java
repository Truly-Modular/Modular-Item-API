package smartin.miapi.item.modular.items;

import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class ModularToolMaterial implements ToolMaterial{
    public static ModularToolMaterial toolMaterial = new ModularToolMaterial();

    @Override
    public int getDurability() {
        return 50;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return 5;
    }

    @Override
    public float getAttackDamage() {
        return 5;
    }

    @Override
    public int getMiningLevel() {
        return 5;
    }

    @Override
    public int getEnchantability() {
        return 15;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }
}
