package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import net.minecraft.item.ItemStack;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.modules.properties.util.DoubleProperty;

public class WaterDragProperty extends DoubleProperty {
    public static String KEY = "water_drag";
    public static WaterDragProperty property;

    public WaterDragProperty() {
        super(KEY);
        property = this;
        MiapiProjectileEvents.MODULAR_PROJECTILE_DATA_TRACKER_INIT.register((projectile, nbtCompound) -> {
            nbtCompound.set(ItemProjectileEntity.WATER_DRAG, (float) getValueSafe(projectile.asItemStack()));
            projectile.waterDrag = (float) getValueSafe(projectile.asItemStack());
            return EventResult.pass();
        });
        MiapiProjectileEvents.MODULAR_PROJECTILE_DATA_TRACKER_SET.register((projectile, nbtCompound) -> {
            nbtCompound.set(ItemProjectileEntity.WATER_DRAG, (float) getValueSafe(projectile.asItemStack()));
            projectile.waterDrag = (float) getValueSafe(projectile.asItemStack());
            return EventResult.pass();
        });
    }

    @Override
    public Double getValue(ItemStack stack) {
        return this.getValueRaw(stack);
    }

    @Override
    public double getValueSafe(ItemStack stack) {
        Double value = getValueRaw(stack);
        return value == null ? 1 : value;
    }
}
