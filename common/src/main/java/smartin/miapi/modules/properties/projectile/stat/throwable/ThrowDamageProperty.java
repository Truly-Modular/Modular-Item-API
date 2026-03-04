package smartin.miapi.modules.properties.projectile.stat.throwable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.attributes.AttributeUtil;
import smartin.miapi.modules.properties.projectile.IsCrossbowShootAble;
import smartin.miapi.modules.properties.projectile.stat.BowStatProperty;
import smartin.miapi.modules.properties.projectile.stat.projectile.ProjectileAccuracyProperty;
import smartin.miapi.modules.properties.projectile.stat.projectile.ProjectileSpeedProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

import java.awt.*;
import java.text.DecimalFormat;

public class ThrowDamageProperty extends BowStatProperty {
    public static final ResourceLocation KEY = Miapi.id("throw_damage");
    public static ThrowDamageProperty property;
    public static DecimalFormat FORMAT = new DecimalFormat("##.##");

    public ThrowDamageProperty() {
        super(KEY, FORMAT, 0.01);
        super.lowerBound = 1.9;
        super.upperBound = 3.1;
        property = this;
        LoreProperty.loreSuppliers.add((itemStack, tooltip, context, tooltipType) -> {
            if (
                    ThrowDamageProperty.property.isPresent(itemStack)
            ) {
                if (IsCrossbowShootAble.canCrossbowShoot(itemStack) && isExtendedTooltip()) {
                    tooltip.add(
                            Component.translatable("miapi.lore.throw.long_arrow",
                                    getToolTipValue(itemStack),
                                    ThrowSpeedProperty.property.getToolTipValue(itemStack),
                                    ProjectileSpeedProperty.property.getToolTipValue(itemStack),
                                    ProjectileAccuracyProperty.property.getToolTipValue(itemStack))
                    );
                } else {
                    tooltip.add(
                            Component.translatable(isExtendedTooltip() ? "miapi.lore.throw.long" : "miapi.lore.throw.short",
                                    getToolTipValue(itemStack),
                                    ThrowSpeedProperty.property.getToolTipValue(itemStack),
                                    ProjectileAccuracyProperty.property.getToolTipValue(itemStack))
                    );
                }
            }
        });
    }

    public Component getToolTipValue(ItemStack stack) {
        if (isPresent(stack)) {
            return super.getToolTipValue(stack);
        } else {
            double value = AttributeUtil.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_DAMAGE.value());
            MutableComponent text = Component.literal(format.format(value));


            if (value < 2) {
                text = text.withStyle(style -> style.withColor(tooltipInverse() ? Color.GREEN.getRGB() : Color.RED.getRGB()));
            } else if (value > 3) {
                text = text.withStyle(style -> style.withColor(tooltipInverse() ? Color.RED.getRGB() : Color.GREEN.getRGB()));
            } else {
                text = text.withStyle(style -> style.withColor(Color.BLUE.getRGB()));
            }
            return text;
        }
    }

    public boolean isPercentStat() {
        return false;
    }

    public static double getDamage(ItemStack itemStack) {
        if (property.isPresent(itemStack)) {
            double damage = property.getData(itemStack).map(DoubleOperationResolvable::getValue).orElse(0.0);
            //remove projectile speed modifier since the projectile will re-apply it
            damage = (1.0 / ProjectileSpeedProperty.getSpeedModifier(itemStack)) * damage;
            return damage;
        } else {
            return AttributeUtil.getActualValue(itemStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_DAMAGE.value());
        }
    }
}
