package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import net.minecraft.item.ItemStack;
import smartin.miapi.events.MiapiEvents;
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
        MiapiEvents.PLAYER_TICK_START.register(playerEntity -> {
            if (playerEntity.isLogicalSideForUpdatingMovement() && playerEntity.age % 40 == 0) {
                double getValue = getForItems(playerEntity.getArmorItems());
                if (getValue > 0.2) {
                    playerEntity.getHungerManager().addExhaustion((float) (getValue / 50.0f));
                }
            }
            return EventResult.pass();
        });
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
