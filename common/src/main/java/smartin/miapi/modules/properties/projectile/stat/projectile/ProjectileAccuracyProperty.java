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

public class ProjectileAccuracyProperty extends BowStatProperty {
    public static final ResourceLocation KEY = Miapi.id("projectile_accuracy");
    public static ProjectileAccuracyProperty property;
    public static DecimalFormat FORMAT = new DecimalFormat("##.##");

    public ProjectileAccuracyProperty() {
        super(KEY, FORMAT, 0.01);
        super.lowerBound = -0.1;
        super.upperBound = 0.1;
        property = this;
    }

    public static double getDivergence(ItemStack itemStack) {
        if (property.isPresent(itemStack)) {
            double accuracy = property.getData(itemStack).map(DoubleOperationResolvable::getValue).orElse(0.0);
            if (accuracy > 0) {
                //when positive, every 100% half the divergence
                return 1 - Math.pow(0.5, (accuracy) / 100.0);
            } else {
                //when negative, every 100% increase by 1%
                return -accuracy * 0.01 + 1;
            }
        } else {
            return Math.pow(12.0, -AttributeUtil.getActualValue(itemStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_ACCURACY.value()));
        }
    }
}
