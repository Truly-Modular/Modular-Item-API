package smartin.miapi.mixin;

import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ProjectileDispenserBehavior.class)
public interface ProjectileDispenserBehaviorAccessor {

    @Invoker("createProjectile")
    ProjectileEntity createProjectileAccessor(World world, Position position, ItemStack itemStack);
}
