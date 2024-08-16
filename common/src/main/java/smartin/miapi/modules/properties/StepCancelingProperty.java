package smartin.miapi.modules.properties;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

public class StepCancelingProperty extends ComplexBooleanProperty {
    public static StepCancelingProperty property;
    public static final ResourceLocation KEY = Miapi.id("step_noise_cancel");

    public StepCancelingProperty() {
        super(KEY, false);
        property = this;
    }

    public static boolean makesStepNoise(Entity entity, boolean old) {
        if (entity instanceof LivingEntity livingEntity && old) {
            double value = property.getForItems(livingEntity.getArmorAndBodyArmorSlots());
            if (value == 0) {
                return old;
            }
            return value < 0;
        }
        return old;
    }
}
