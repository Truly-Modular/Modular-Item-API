package smartin.miapi.modules.properties.projectile.stat.projectile;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.modules.properties.attributes.AttributeUtil;
import smartin.miapi.modules.properties.projectile.stat.BowStatProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

import java.text.DecimalFormat;

public class ProjectileSpeedProperty extends BowStatProperty {
    public static final ResourceLocation KEY = Miapi.id("projectile_speed");
    public static ProjectileSpeedProperty property;
    public static DecimalFormat FORMAT = new DecimalFormat("##.##");

    public ProjectileSpeedProperty() {
        super(KEY, FORMAT, 0.01);
        super.lowerBound = -0.1;
        super.upperBound = 0.1;
        property = this;
    }

    public static double getSpeedModifier(ItemStack itemStack) {
        if (property.isPresent(itemStack)) {
            double speed = property.getData(itemStack).map(DoubleOperationResolvable::getValue).orElse(0.0);
            if (speed < 0) {
                //when negative, every 100% half the divergence
                return Math.pow(0.5, -speed / 100.0);
            } else {
                //when postive, every 100% increase by 1%
                return speed * 0.01 + 1;
            }
        } else {
            return Math.max(0.1, 1 + AttributeUtil.getActualValue(itemStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_SPEED.value()));
        }
    }
}
