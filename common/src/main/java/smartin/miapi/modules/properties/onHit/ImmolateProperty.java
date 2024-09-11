package smartin.miapi.modules.properties.onHit;

import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * This property is intended for implementing an "immolate" effect, which typically involves setting a target on fire or inflicting burn damage.
 * The specifics of this property are still under development, and the exact functionality will be defined later.
 *
 * @header Immolate Property
 * @path /data_types/properties/on_hit/immolate
 * @description_start
 * This is currently not implemented
 * @description_end
 * @data value:
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
