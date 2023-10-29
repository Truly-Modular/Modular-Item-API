package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import smartin.miapi.modules.properties.util.DoubleProperty;
import smartin.miapi.registries.RegistryInventory;

public class CryoProperty extends DoubleProperty {
    public static final String KEY = "cryo";
    public static CryoProperty property;

    public CryoProperty() {
        super(KEY);
        property = this;
        PlayerEvent.ATTACK_ENTITY.register((player, level, target, hand, result) -> {
            if (player.getWorld().isClient()) {
                return EventResult.pass();
            }
            double strength = getForItems(player.getHandItems());
            if (strength > 0 && target instanceof LivingEntity living) {
                int potionStrength = (int) Math.ceil(strength / 3);
                int potionLength = (int) (strength * 20 + 40);
                StatusEffectInstance instance = new StatusEffectInstance(RegistryInventory.cryoStatusEffect, potionLength, potionStrength);
                living.addStatusEffect(instance, player);
            }
            return EventResult.pass();
        });
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
