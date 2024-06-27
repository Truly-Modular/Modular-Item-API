package smartin.miapi.mixin;

import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ThrownPotion.class)
public interface ThrowablePotionItemAccessor {

    @Invoker("onCollision")
    void onCollisionMixin(HitResult hitResult);
}
