package smartin.miapi.modules.properties.projectile;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * This Property manages speedloss of {@link smartin.miapi.entity.ItemProjectileEntity} of Modular Items
 * @header Air Drag Property
 * @path /data_types/properties/projectile/air_drag
 * @description_start
 * The air drag affects the speed loss of projectiles as they travel through the air. The drag is represented as a double
 * value, 1.0 means no loss, 0.0 means full loss of velocity.
 * @description_end
 * @data air_drag:multiplied to air speed each tick.
 */

public class AirDragProperty extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("air_drag");
    public static AirDragProperty property;

    public AirDragProperty() {
        super(KEY);
        property = this;
    }

    public double getValueSafe(ItemStack stack) {
        return getValue(stack).orElse(1.0);
    }
}
