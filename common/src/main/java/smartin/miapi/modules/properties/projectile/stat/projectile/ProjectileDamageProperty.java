package smartin.miapi.modules.properties.projectile.stat.projectile;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.attributes.AttributeUtil;
import smartin.miapi.modules.properties.projectile.stat.BowStatProperty;
import smartin.miapi.modules.properties.projectile.stat.throwable.ThrowDamageProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

import java.awt.*;
import java.text.DecimalFormat;

public class ProjectileDamageProperty extends BowStatProperty {
    public static final ResourceLocation KEY = Miapi.id("projectile_damage");
    public static ProjectileDamageProperty property;
    public static DecimalFormat FORMAT = new DecimalFormat("##.##");

    public ProjectileDamageProperty() {
        super(KEY, FORMAT, 0.01);
        super.lowerBound = -0.1;
        super.upperBound = 0.1;
        property = this;
        LoreProperty.loreSuppliers.add((itemStack, tooltip, context, tooltipType) -> {
            if (
                    (
                            ProjectileAccuracyProperty.property.isPresent(itemStack) ||
                            ProjectileSpeedProperty.property.isPresent(itemStack) ||
                            ProjectileDamageProperty.property.isPresent(itemStack))
                    && !ThrowDamageProperty.property.isPresent(itemStack)
            ) {

                tooltip.add(
                        Component.translatable(isExtendedTooltip() ? "miapi.lore.projectile.long" : "miapi.lore.projectile.short",
                                getToolTipValue(itemStack),
                                ProjectileSpeedProperty.property.getToolTipValue(itemStack),
                                ProjectileAccuracyProperty.property.getToolTipValue(itemStack))
                );
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

    public static double getDamage(ItemStack itemStack) {
        if (property.isPresent(itemStack)) {
            double damage = property.getData(itemStack).map(DoubleOperationResolvable::getValue).orElse(0.0);
            if (damage < 0) {
                //when positive, every 100% half the divergence
                return (1.0 - Math.pow(0.5, -damage / 100.0)) * 2;
            } else {
                //when negative, every 100% increase by 1%
                return (1 + damage * 0.01) * 2;
            }
        } else if (ThrowDamageProperty.property.isPresent(itemStack)) {
            return ThrowDamageProperty.getDamage(itemStack);
        } else {
            return AttributeUtil.getActualValue(itemStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_DAMAGE.value());
        }
    }
}
