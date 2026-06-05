package smartin.miapi.item.modular.items.bows.neoforge;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;

import java.util.function.Predicate;

public class ModularCrossbowImpl {


    public static Predicate<ItemStack> getAllSupportedProjectilesWithApi(ProjectileWeaponItem weaponItem, ItemStack weapon) {
        return weaponItem.getAllSupportedProjectiles(weapon);
    }
}
