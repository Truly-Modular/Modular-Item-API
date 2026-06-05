package smartin.miapi.forge.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import smartin.miapi.item.modular.items.bows.ModularCrossbow;
import smartin.miapi.modules.properties.projectile.AllowedProjectileProperty;
import smartin.miapi.modules.properties.projectile.IsCrossbowShootAble;

import java.util.function.Predicate;

@Mixin(ModularCrossbow.class)
public class ModularCrossBowMixin {

    @SuppressWarnings("unused")
    //overwrite froge method via mixin.
    //i hate this workflow lmao
    public Predicate<ItemStack> getAllSupportedProjectiles(ItemStack weaponStack) {
        ModularCrossbow crossbow = (ModularCrossbow) (Object) this;
        return AllowedProjectileProperty
                .getAllowedProjectiles(
                        crossbow,
                        weaponStack, () -> getAllSupportedProjectiles(weaponStack).or(IsCrossbowShootAble::canCrossbowShoot));
    }
}
