package smartin.miapi.modules.properties;

import com.redpxnda.nucleus.math.MathUtil;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import smartin.miapi.item.FakeEnchantment;
import smartin.miapi.modules.properties.util.DoubleProperty;

public class LeechingProperty extends DoubleProperty {
    public static final String KEY = "leeching";
    public static LeechingProperty property;


    public LeechingProperty() {
        super(KEY);
        property = this;
        FakeEnchantment.addTransformer(Enchantments.FIRE_PROTECTION, (stack, level) -> {
            int toLevel = (int) Math.ceil(getValueSafe(stack) / 4) + level;
            return Math.min(toLevel + level, Math.max(4, level));
        });
        TickEvent.PLAYER_POST.register(player -> {
            if(player.getWorld().isClient()){
                return;
            }
            if (player.age % 20 * 45 == 0) {
                for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                    ItemStack itemStack = player.getEquippedStack(equipmentSlot);
                    double strength = getValueSafe(itemStack);
                    if (strength > 0) {
                        double chance = Math.min(1, strength * 0.05 + 0.05);
                        if (MathUtil.random(0d, 1d) < chance) {
                            int xpPoints = (int) MathUtil.random(1 + strength, 5 + strength);
                            player.addExperience(-xpPoints);
                            if (itemStack.isDamaged() && itemStack.isDamageable()) {
                                itemStack.damage(-xpPoints, player, p -> p.equipStack(equipmentSlot, itemStack));
                            }
                        }
                    }
                }
            }
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
