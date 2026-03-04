package smartin.miapi.modules.properties.projectile.stat.throwable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.modules.properties.attributes.AttributeUtil;
import smartin.miapi.modules.properties.projectile.stat.BowStatProperty;
import smartin.miapi.modules.properties.projectile.stat.projectile.ProjectileSpeedProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

import java.text.DecimalFormat;

public class ThrowSpeedProperty extends BowStatProperty {
    public static final ResourceLocation KEY = Miapi.id("throw_speed");
    public static ThrowSpeedProperty property;
    public static DecimalFormat FORMAT = new DecimalFormat("##.##");

    public ThrowSpeedProperty() {
        super(KEY, FORMAT, 0.01);
        super.lowerBound = 1.9;
        super.upperBound = 2.1;
        property = this;
    }


    public boolean isPercentStat() {
        return false;
    }


    public static double getThrowSpeed(ItemStack itemStack) {
        if (property.isPresent(itemStack)) {
            double speed = property.getData(itemStack).map(DoubleOperationResolvable::getValue).orElse(0.0);

            //remove projectile speed modifier since the projectile will re-apply it
            speed = (1.0 / ProjectileSpeedProperty.getSpeedModifier(itemStack)) * speed;
            return speed;
        } else {
            return Math.max(0.1, AttributeUtil.getActualValue(itemStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_SPEED.value()));
        }
    }
}
