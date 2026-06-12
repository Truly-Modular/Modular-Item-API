package smartin.miapi.forge.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import smartin.miapi.modules.properties.projectile.AllowedProjectileProperty;

import java.util.function.Predicate;

@Mixin(Player.class)
public class PlayerMixin {

    @WrapOperation(
            method = "Lnet/minecraft/world/entity/player/Player;getProjectile(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ProjectileWeaponItem;getSupportedHeldProjectiles(Lnet/minecraft/world/item/ItemStack;)Ljava/util/function/Predicate;"
            )
    )
    private Predicate<ItemStack> miapi$redirectHeldProjectiles(
            ProjectileWeaponItem weapon,
            ItemStack weaponStack,
            Operation<Predicate<ItemStack>> original
    ) {
        return AllowedProjectileProperty.getAllowedHandProjectiles(weapon, weaponStack, () -> original.call(weapon, weaponStack));
    }

    @WrapOperation(
            method = "Lnet/minecraft/world/entity/player/Player;getProjectile(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ProjectileWeaponItem;getAllSupportedProjectiles(Lnet/minecraft/world/item/ItemStack;)Ljava/util/function/Predicate;"
            )
    )
    private Predicate<ItemStack> miapi$redirectSupportedProjectiles(
            ProjectileWeaponItem weapon,
            ItemStack weaponStack,
            Operation<Predicate<ItemStack>> original
    ) {
        return AllowedProjectileProperty.getAllowedProjectiles(weapon, weaponStack, () -> original.call(weapon, weaponStack));
    }
}
