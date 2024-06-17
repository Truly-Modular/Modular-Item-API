package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import net.minecraft.item.ItemStack;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * This property allows for armor penetration, so weapons can igonre some armor
 */
public class WaterGravityProperty extends DoubleProperty {
    public static final String KEY = "water_gravity";
    public static WaterGravityProperty property;

    public WaterGravityProperty() {
        super(KEY);
        property = this;
        MiapiEvents.PLAYER_TICK_START.register((player -> {
            if (player.isSubmergedInWater() && player.isLogicalSideForUpdatingMovement()) {
                double speed = property.getForItems(player.getEquippedItems()) / 100;
                if (player.getVelocity().y < speed) {
                    player.addVelocity(0, -speed / 20, 0);
                }
            }
            return EventResult.pass();
        }));
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
