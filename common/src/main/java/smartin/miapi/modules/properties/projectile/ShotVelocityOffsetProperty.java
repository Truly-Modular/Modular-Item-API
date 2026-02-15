package smartin.miapi.modules.properties.projectile;

import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * @header Shot Velocity Offset
 * @path /data_types/properties/projectile/shot_velocity
 * @description_start
 * This property is a complex double allowing to adjust the speed a projectile is shot from a crossbow.
 * THis exists to allow a difference in shot and thrown velocity for items than can do both
 * @description_end
 * @data shot_velocity : offset to initial velocity.
 */
public class ShotVelocityOffsetProperty extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("shot_velocity");
    public static ShotVelocityOffsetProperty property;

    public ShotVelocityOffsetProperty() {
        super(KEY);
        property = this;
    }
}
