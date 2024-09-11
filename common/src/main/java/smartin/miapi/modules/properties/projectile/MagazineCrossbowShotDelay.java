package smartin.miapi.modules.properties.projectile;

import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * This property defines the delay between shots when using a crossbow with a magazine that allows rapid fire.
 * @header Rapid Fire Crossbow Delay Property
 * @path /data_types/properties/projectile/rapid_fire_crossbow_delay
 * @description_start
 * The Rapid Fire Crossbow Delay Property controls the time delay between consecutive shots when using a crossbow
 * configured for rapid fire. The delay is specified as a double value, where lower values allow faster firing.
 * @description_end
 * @data rapid_fire_crossbow_delay: A double value representing the time (in ticks) between consecutive shots.
 */
public class MagazineCrossbowShotDelay extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("rapid_fire_crossbow_delay");
    public static MagazineCrossbowShotDelay property;

    public MagazineCrossbowShotDelay() {
        super(KEY);
        property = this;
    }
}
