package smartin.miapi.forge.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.projectile.AllowedProjectileProperty;

import java.util.function.Predicate;

@Mixin(ProjectileWeaponItem.class)
public class ModularCrossBowMixin {

    @ModifyReturnValue(method = "getAllSupportedProjectiles(Lnet/minecraft/world/item/ItemStack;)Ljava/util/function/Predicate;", at = @At("RETURN"))
    public Predicate<ItemStack> miapi$getAllSupportedProjectiles(Predicate<ItemStack> original, ItemStack weaponStack) {
        ProjectileWeaponItem crossbow = (ProjectileWeaponItem) (Object) this;
        if (crossbow instanceof ModularItem) {
            return AllowedProjectileProperty
                    .getAllowedProjectiles(
                            crossbow,
                            weaponStack, () -> original);
        }
        return original;
    }

    @ModifyReturnValue(method = "getSupportedHeldProjectiles(Lnet/minecraft/world/item/ItemStack;)Ljava/util/function/Predicate;", at = @At("RETURN"))
    public Predicate<ItemStack> miapi$getAllHeldSupportedProjectiles(Predicate<ItemStack> original, ItemStack weaponStack) {
        ProjectileWeaponItem crossbow = (ProjectileWeaponItem) (Object) this;
        if (crossbow instanceof ModularItem) {
            return AllowedProjectileProperty
                    .getAllowedHandProjectiles(
                            crossbow,
                            weaponStack, () -> original);
        }
        return original;
    }
}
