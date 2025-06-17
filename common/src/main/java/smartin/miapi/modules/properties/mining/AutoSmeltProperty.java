package smartin.miapi.modules.properties.mining;

import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

public class AutoSmeltProperty extends ComplexBooleanProperty {
    public static AutoSmeltProperty property;
    public static ResourceLocation KEY = Miapi.id("autosmelt");

    public AutoSmeltProperty() {
        super(KEY, false);
        property = this;
    }
}
