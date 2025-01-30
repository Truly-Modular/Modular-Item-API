package smartin.miapi.modules.properties.projectile;

import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

/**
 * This property marks a Projectile to make no sound if set to false
 * @header Crossbow Shootable Property
 * @path /data_types/properties/projectile/projectile_impact_sound
 * @description_start
 *
 * @description_end
 * @data crossbow_ammunition: A boolean value indicating whether the projectile makes an impact sound
 */

public class MakesImpactSoundProperty extends ComplexBooleanProperty {
    public static final ResourceLocation KEY = Miapi.id("projectile_impact_sound");
    public static MakesImpactSoundProperty property;

    public MakesImpactSoundProperty() {
        super(KEY, true);
        property = this;
    }
}
