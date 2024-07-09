package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * This property manages the speedloss of Projectiles inside Water
 */
public class WaterDragProperty extends DoubleProperty {
    public static final String KEY = "water_drag";
    public static WaterDragProperty property;

    public WaterDragProperty() {
        super(KEY);
        property = this;
        MiapiProjectileEvents.MODULAR_PROJECTILE_DATA_TRACKER_INIT.register((projectile, nbtCompound) -> {
            float waterDrag = getValue(projectile.getPickupItem()).orElse(1.0).floatValue();
            nbtCompound.set(ItemProjectileEntity.WATER_DRAG, waterDrag);
            projectile.waterDrag = waterDrag;
            return EventResult.pass();
        });
        MiapiProjectileEvents.MODULAR_PROJECTILE_DATA_TRACKER_SET.register((projectile, nbtCompound) -> {
            float waterDrag = getValue(projectile.getPickupItem()).orElse(1.0).floatValue();
            nbtCompound.set(ItemProjectileEntity.WATER_DRAG, waterDrag);
            projectile.waterDrag = waterDrag;
            return EventResult.pass();
        });
    }
}
