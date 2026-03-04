package smartin.miapi.modules.properties.projectile.stat.bow;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.modules.properties.attributes.AttributeUtil;
import smartin.miapi.modules.properties.projectile.stat.BowStatProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

import java.text.DecimalFormat;

public class BowSpeedProperty extends BowStatProperty {
    public static final ResourceLocation KEY = Miapi.id("bow_speed");
    public static BowSpeedProperty property;
    public static DecimalFormat FORMAT = new DecimalFormat("##.##");

    public BowSpeedProperty() {
        super(KEY, FORMAT, 0.01);
        property = this;
    }

    public static double getSpeedModifier(ItemStack itemStack, double baseValue) {
        if (property.isPresent(itemStack)) {
            double speed = property.getData(itemStack).map(DoubleOperationResolvable::getValue).orElse(0.0);
            if (speed < 0) {
                //when positive, every 100% half the divergence
                return (1.0 - Math.pow(0.5, -speed / 100.0)) * baseValue;
            } else {
                //when negative, every 100% increase by 1%
                return (speed * 0.01 + 1) * baseValue;
            }
        } else {
            return Math.max(0.1, AttributeUtil.getActualValue(itemStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_SPEED.value()));
        }
    }
}
