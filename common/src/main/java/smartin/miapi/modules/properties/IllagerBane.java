package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.mob.VindicatorEntity;
import net.minecraft.item.ItemStack;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * this property increases damage to Raid type mods
 */
public class IllagerBane extends DoubleProperty {
    ///TODO:look how to better detect those entitys, maybe a tag and look into mod compat as well
    public static final String KEY = "illagerBane";
    public static IllagerBane property;

    public IllagerBane() {
        super(KEY);
        property = this;
        MiapiEvents.LIVING_HURT.register((livingHurtEvent) -> {
            ItemStack itemStack = livingHurtEvent.getCausingItemStack();
            Double value = getValue(itemStack);
            if (value != null && isIllagerType(livingHurtEvent.livingEntity)) {
                livingHurtEvent.amount += value;
            }
            return EventResult.pass();
        });
    }

    public static boolean isIllagerType(LivingEntity living) {
        if(
                        living instanceof PillagerEntity ||
                        living instanceof VexEntity ||
                        living instanceof VindicatorEntity ||
                        living instanceof RavagerEntity
        ){
            return true;
        }
        return false;
    }

    @Override
    public Double getValue(ItemStack stack) {
        return getValueRaw(stack);
    }

    @Override
    public double getValueSafe(ItemStack stack) {
        return getValueSafeRaw(stack);
    }
}
