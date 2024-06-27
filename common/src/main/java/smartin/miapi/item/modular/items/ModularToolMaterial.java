package smartin.miapi.item.modular.items;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public class ModularToolMaterial implements Tier{
    public static ModularToolMaterial toolMaterial = new ModularToolMaterial();

    @Override
    public int getUses() {
        return 50;
    }

    @Override
    public float getSpeed() {
        return 5;
    }

    @Override
    public float getAttackDamageBonus() {
        return 5;
    }

    @Override
    public int getMiningLevel() {
        return 5;
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }
}
