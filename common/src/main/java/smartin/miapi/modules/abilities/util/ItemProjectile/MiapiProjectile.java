package smartin.miapi.modules.abilities.util.ItemProjectile;

import net.minecraft.item.ItemStack;

public interface MiapiProjectile {
    boolean thisIsCause();
    ItemStack responsibleItem();
    ItemStack getThisAsItem();
}
