package smartin.miapi.modules.properties.projectile.stat;

import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.properties.attributes.AttributeUtil;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.DoubleProperty;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class BowStatProperty extends DoubleProperty {
    public DecimalFormat format;
    public double roundTo;
    public double lowerBound = -0.25;
    public double upperBound = 0.25;

    protected BowStatProperty(ResourceLocation cacheKey, DecimalFormat format, double roundTarget) {
        super(cacheKey);
        this.format = format;
        roundTo = roundTarget;
    }

    public boolean isPercentStat() {
        return true;
    }

    public double baseValue() {
        return 1.0;
    }

    public boolean tooltipInverse() {
        return false;
    }

    public boolean isPresent(ItemStack stack) {
        return getData(stack).isPresent();
    }

    public Component getToolTipValue(ItemStack stack) {
        double value = roundToNearest(
                getData(stack).map(DoubleOperationResolvable::getValue).orElse(0.0),
                roundTo
        );

        MutableComponent text = Component.literal(format.format(value));
        if (isPercentStat()) {
            if (value > 0) {
                text = Component.literal("+").append(text);
            }
            text.append(Component.literal("%"));
        }


        if (value < lowerBound) {
            text = text.withStyle(style -> style.withColor(tooltipInverse() ? Color.GREEN.getRGB() : Color.RED.getRGB()));
        } else if (value > upperBound) {
            text = text.withStyle(style -> style.withColor(tooltipInverse() ? Color.RED.getRGB() : Color.GREEN.getRGB()));
        } else {
            text = text.withStyle(style -> style.withColor(Color.BLUE.getRGB()));
        }

        return text;
    }

    public static double getValue(ItemStack itemStack, Holder<Attribute> attribute, BowStatProperty property) {
        if (property.isPresent(itemStack)) {
            if (property.isPercentStat()) {
                return (property.baseValue() *
                        property.getData(itemStack).map(DoubleOperationResolvable::getValue).orElse(0.0) * 0.01);
            } else {
                return property.getData(itemStack).map(DoubleOperationResolvable::getValue).orElse(0.0);
            }
        }
        return AttributeUtil.getActualValue(itemStack, EquipmentSlot.MAINHAND, attribute.value());
    }

    public static double roundToNearest(double value, double roundTo) {
        BigDecimal bdValue = new BigDecimal(value);
        BigDecimal bdRoundTo = new BigDecimal(roundTo);

        // Divide, round, and then multiply back
        BigDecimal divided = bdValue.divide(bdRoundTo, 0, RoundingMode.HALF_UP);
        BigDecimal result = divided.multiply(bdRoundTo);

        return result.doubleValue();
    }

    public static boolean isExtendedTooltip() {
        if (Platform.getEnv() == EnvType.CLIENT) {
            return isExtendedTooltipClient();
        } else {
            return true;
        }
    }

    private static boolean isExtendedTooltipClient() {
        return Screen.hasShiftDown();
    }
}
