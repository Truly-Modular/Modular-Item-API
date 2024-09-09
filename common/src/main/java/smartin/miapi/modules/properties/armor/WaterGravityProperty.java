package smartin.miapi.modules.properties.armor;

import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * This property allows for armor penetration, so weapons can igonre some armor
 * @header Water Gravity
 * @description_start
 * This property increases Gravity while under water, used to make players sink faster
 * @desciption_end
 * @path /data_types/properties/armor/water_gravity
 * @data water_gravity:a Double Resolvable
 */
public class WaterGravityProperty extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("water_gravity");
    public static WaterGravityProperty property;

    public WaterGravityProperty() {
        super(KEY);
        property = this;
        MiapiEvents.PLAYER_TICK_START.register((player -> {
            if (player.isUnderWater() && player.isControlledByLocalInstance()) {
                double speed = property.getForItems(player.getAllSlots()) / 100;
                if (player.getDeltaMovement().y < speed) {
                    player.push(0, -speed / 20, 0);
                }
            }
            return EventResult.pass();
        }));
    }
}
