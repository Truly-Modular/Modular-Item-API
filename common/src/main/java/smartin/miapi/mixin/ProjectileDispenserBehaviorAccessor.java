package smartin.miapi.mixin;

import net.minecraft.core.Position;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ProjectileDispenseBehavior.class)
public interface ProjectileDispenserBehaviorAccessor {

    @Invoker("createProjectile")
    Projectile createProjectileAccessor(Level world, Position position, ItemStack itemStack);
}
