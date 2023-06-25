package smartin.miapi.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {

    @Accessor("attacking")
    void attacking(LivingEntity attacking);

    /*
    @Invoker("damageArmor")
    void damageArmor(DamageSource source, float amount);

     */
}
