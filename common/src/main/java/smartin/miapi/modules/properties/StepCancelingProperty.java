package smartin.miapi.modules.properties;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

public class StepCancelingProperty extends ComplexBooleanProperty {
    public static StepCancelingProperty property;
    public static String KEY = "step_noise_cancel";

    public StepCancelingProperty() {
        super(KEY, false);
        property = this;
    }

    public static boolean makesStepNoise(Entity entity, boolean old) {
        if (entity instanceof LivingEntity livingEntity && old) {
            double value = property.getForItems(livingEntity.getItemsEquipped());
            if (value == 0) {
                return true;
            }
            return value < 0;
        }
        return old;
    }
}
