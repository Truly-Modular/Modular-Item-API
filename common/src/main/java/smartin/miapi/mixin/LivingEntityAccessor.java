package smartin.miapi.mixin;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {

    @Accessor("attacking")
    void attacking(LivingEntity attacking);

    /*
    @Invoker("damageArmor")
    void damageArmor(DamageSource source, float amount);

     */
}
