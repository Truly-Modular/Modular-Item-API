package smartin.miapi.modules.properties.projectile;

import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * This Property manages speed loss of {@link smartin.miapi.entity.ItemProjectileEntity} of Modular Items
 * @header Water Drag Property
 * @path /data_types/properties/projectile/water_drag
 * @description_start
 * The water drag affects the speed loss of projectiles when they travel through water. The drag is represented as a double
 * value, where 1.0 means no speed loss and 0.0 means full loss of velocity in water.
 * @description_end
 * @data water_drag: Multiplied to the projectile's speed each tick when underwater.
 */

public class WaterDragProperty extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("water_drag");
    public static WaterDragProperty property;

    public WaterDragProperty() {
        super(KEY);
        property = this;
        /*
        MiapiProjectileEvents.MODULAR_PROJECTILE_DATA_TRACKER_INIT.register((projectile, nbtCompound) -> {
            float waterDrag = getValue(projectile.getPickupItem()).orElse(1.0).floatValue();
            nbtCompound.set(ItemProjectileEntity.WATER_DRAG, waterDrag);
            projectile.waterDrag = waterDrag;
            return EventResult.pass();
        });

         */
        MiapiProjectileEvents.MODULAR_PROJECTILE_DATA_TRACKER_SET.register((projectile, nbtCompound) -> {
            float waterDrag = getValue(projectile.getPickupItem()).orElse(1.0).floatValue();
            nbtCompound.set(ItemProjectileEntity.WATER_DRAG, waterDrag);
            projectile.waterDrag = waterDrag;
            return EventResult.pass();
        });
    }
}
