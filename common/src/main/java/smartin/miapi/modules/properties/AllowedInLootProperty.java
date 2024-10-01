package smartin.miapi.modules.properties;

import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

/**
 * @header Allowed in Loot Property
 * @path /data_types/properties/allowed_loot
 * @description_start
 * if this modular can be found in randomized loot. should be turned off for rare/special modules.
 * @description_end
 * @data value:a boolean value, default is true
 * */
public class AllowedInLootProperty extends ComplexBooleanProperty {
    public final static ResourceLocation KEY = Miapi.id("allowed_in_loot");
    public static AllowedInLootProperty property;

    public AllowedInLootProperty() {
        super(KEY, true);
        property = this;
    }
}
