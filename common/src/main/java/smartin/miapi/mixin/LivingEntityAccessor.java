package smartin.miapi.mixin;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

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
}
