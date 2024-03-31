package smartin.miapi.modules.properties;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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
        if (entity instanceof PlayerEntity playerEntity && playerEntity.isLogicalSideForUpdatingMovement()) {
            double getValue = property.getForItems(playerEntity.getArmorItems());
            if (playerEntity.getRandom().nextDouble() < 0.15 && getValue > 0.2) {
                playerEntity.getHungerManager().addExhaustion((float) (getValue / 100.0f));
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
