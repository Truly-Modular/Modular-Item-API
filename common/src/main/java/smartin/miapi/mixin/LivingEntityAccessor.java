package smartin.miapi.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {

    @Accessor("lastHurtMob")
    void attacking(LivingEntity attacking);

    @Invoker("hurtArmor")
    void callDamageArmor(DamageSource source, float amount);

    @Accessor("lastHurt")
    float getLastDamageTaken();

    @Invoker("collectEquipmentChanges")
    Map<EquipmentSlot, ItemStack> callGetEquipmentChanges();

    @Accessor("lastHurtByMobTimestamp")
    int getLastAttackedTime();
}
