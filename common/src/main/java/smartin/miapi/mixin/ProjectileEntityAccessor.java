package smartin.miapi.mixin;

import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ProjectileEntity.class)
public interface ProjectileEntityAccessor {

    @Invoker("onCollision")
    void onCollisionMixin(HitResult hitResult);
}
