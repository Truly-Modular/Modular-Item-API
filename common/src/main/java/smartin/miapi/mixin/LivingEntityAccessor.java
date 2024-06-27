package smartin.miapi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {

    @Accessor("attacking")
    void attacking(LivingEntity attacking);

    @Invoker
    void callDamageArmor(DamageSource source, float amount);

    @Accessor
    float getLastDamageTaken();

    @Invoker
    Map<EquipmentSlot, ItemStack> callGetEquipmentChanges();

    @Accessor
    int getLastAttackedTime();
}
