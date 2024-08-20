package smartin.miapi.modules.properties.onHit;

import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * This property is arson.
 */
public class ImmolateProperty extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("immolate");
    public static ImmolateProperty property;


    //TODO:reimplement new Immolate
    public ImmolateProperty() {
        super(KEY);
        property = this;
    }
}
