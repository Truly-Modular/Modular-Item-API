package smartin.miapi.forge.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import smartin.miapi.modules.properties.projectile.AllowedProjectileProperty;

import java.util.function.Predicate;

@Mixin(Player.class)
public class PlayerMixin {

    @Redirect(
            method = "Lnet/minecraft/world/entity/player/Player;getProjectile(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ProjectileWeaponItem;getSupportedHeldProjectiles(Lnet/minecraft/world/item/ItemStack;)Ljava/util/function/Predicate;"
            )
    )
    private Predicate<ItemStack> miapi$redirectHeldProjectiles(
            ProjectileWeaponItem weapon,
            ItemStack weaponStack
    ) {
        return AllowedProjectileProperty.getAllowedHandProjectiles(weapon, weaponStack, weapon::getSupportedHeldProjectiles);
    }

    @Redirect(
            method = "Lnet/minecraft/world/entity/player/Player;getProjectile(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ProjectileWeaponItem;getAllSupportedProjectiles(Lnet/minecraft/world/item/ItemStack;)Ljava/util/function/Predicate;"
            )
    )
    private Predicate<ItemStack> miapi$redirectSupportedProjectiles(
            ProjectileWeaponItem weapon,
            ItemStack weaponStack
    ) {
        return AllowedProjectileProperty.getAllowedProjectiles(weapon, weaponStack, weapon::getAllSupportedProjectiles);
    }
}
