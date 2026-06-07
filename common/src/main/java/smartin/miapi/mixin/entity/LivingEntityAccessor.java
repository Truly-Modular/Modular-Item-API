package smartin.miapi.mixin.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {

    @Accessor("lastHurtMob")
    void getMiapiLastHurt(LivingEntity attacking);

    @Invoker("hurtArmor")
    void callMiapiDamageArmor(DamageSource source, float amount);

    @Accessor("lastHurt")
    float getMiapiLastDamageTaken();

    @Accessor("lastHurtByMobTimestamp")
    int getMiapiLastAttackedTime();

    @Invoker("isAffectedByFluids")
    boolean callMiapiIsAffectedByFluidsMiapi();
}
