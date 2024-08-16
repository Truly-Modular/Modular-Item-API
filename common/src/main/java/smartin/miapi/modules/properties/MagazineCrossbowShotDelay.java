package smartin.miapi.modules.properties;

import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.DoubleProperty;

public class MagazineCrossbowShotDelay extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("rapid_fire_crossbow_delay");
    public static MagazineCrossbowShotDelay property;

    public MagazineCrossbowShotDelay() {
        super(KEY);
        property = this;
    }
}
