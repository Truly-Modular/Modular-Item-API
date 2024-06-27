package smartin.miapi.modules.properties;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * This property adds a passive Food drain similar to sprinting
 */
public class ExhaustionProperty extends DoubleProperty {
    public static final String KEY = "food_exhaustion";
    public static ExhaustionProperty property;

    public ExhaustionProperty() {
        super(KEY);
        property = this;
    }

    public static void step(Entity entity){
        if (entity instanceof Player playerEntity && playerEntity.isControlledByLocalInstance()) {
            double getValue = property.getForItems(playerEntity.getArmorSlots());
            if (playerEntity.getRandom().nextDouble() < 0.05 && getValue > 0.2) {
                playerEntity.getFoodData().addExhaustion((float) (getValue / 100.0f));
            }
        }
    }

    @Override
    public Double getValue(ItemStack stack) {
        return this.getValueRaw(stack);
    }

    @Override
    public double getValueSafe(ItemStack stack) {
        return getValueSafeRaw(stack);
    }
}
