package smartin.miapi.modules.properties.render;

import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.DoubleProperty;

public class AlphaOverwriteProperty extends DoubleProperty {
    public static ResourceLocation KEY = Miapi.id("alpha_overwrite");
    public static AlphaOverwriteProperty property;

    public AlphaOverwriteProperty() {
        super(KEY);
        property = this;
    }
}
