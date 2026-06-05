package smartin.miapi.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import smartin.miapi.modules.properties.inventory.features.AutoPickupFeatureType;
import smartin.miapi.modules.properties.inventory.features.IsAmmoFeatureType;
import smartin.miapi.modules.properties.projectile.AllowedProjectileProperty;

import java.util.function.Predicate;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Redirect(
            method = "getProjectile",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ProjectileWeaponItem;getSupportedHeldProjectiles()Ljava/util/function/Predicate;"
            )
    )
    private Predicate<ItemStack> miapi$redirectHeldProjectiles(
            ProjectileWeaponItem weapon,
            ItemStack weaponStack
    ) {
        return AllowedProjectileProperty.getAllowedHandProjectiles(weapon, weaponStack, weapon::getSupportedHeldProjectiles);
    }

    @WrapMethod(method = "getProjectile")
    private ItemStack miapi$getProjectile(
            ItemStack weapon,
            Operation<ItemStack> original
    ) {
        Player player = (Player) (Object) this;

        ItemStack found = IsAmmoFeatureType.findStackBeforeVanilla(player,weapon);
        if (!found.isEmpty()) {
            return found;
        }

        ItemStack result = original.call(weapon);

        if (result.isEmpty()) {
            result = IsAmmoFeatureType.findStackAfterVanilla(player,weapon);
            return result;
        }

        return result;
    }

    @WrapMethod(
            method = "addItem(Lnet/minecraft/world/item/ItemStack;)Z"
    )
    private boolean miapi$pickupPrio(ItemStack stack, Operation<Boolean> original) {
        Player player = (Player) (Object) this;
        stack = AutoPickupFeatureType.tryPickUpBeforeInventory(stack, player);
        if (stack == null) return true;
        return original.call(stack);
    }

    @Redirect(
            method = "getProjectile",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ProjectileWeaponItem;getAllSupportedProjectiles()Ljava/util/function/Predicate;"
            )
    )
    private Predicate<ItemStack> miapi$redirectSupportedProjectiles(
            ProjectileWeaponItem weapon,
            ItemStack weaponStack
    ) {
        return AllowedProjectileProperty.getAllowedProjectiles(weapon, weaponStack, weapon::getAllSupportedProjectiles);
    }
}
