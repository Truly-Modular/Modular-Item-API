package smartin.miapi.modules.properties.projectile;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.util.DoubleProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * @header Air Drag Property
 * @path /data_types/properties/projectile/air_drag
 * @description_start This Property manages the Draw time of Modular Bows and Crossbows in seconds.
 * This is a Double Resolvable, so different operations are possible
 * @description_end
 * @data value:the drawtime in seconds.
 */
public class DrawTimeProperty extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("draw_time");
    public static DrawTimeProperty property;
    public static DecimalFormat FORMAT = new DecimalFormat("##.##");

    public DrawTimeProperty() {
        super(KEY);
        property = this;
        LoreProperty.loreSuppliers.add((itemStack, tooltip, context, tooltipType) -> {
            if (getData(itemStack).isPresent()) {
                double drawTime = roundToNearest(getData(itemStack).get().getValue(), 0.05);
                tooltip.add(2, Component.translatable("miapi.lore.draw_time.tooltip", FORMAT.format(drawTime)));
            }
        });
    }

    public static double roundToNearest(double value, double roundTo) {
        BigDecimal bdValue = new BigDecimal(value);
        BigDecimal bdRoundTo = new BigDecimal(roundTo);

        // Divide, round, and then multiply back
        BigDecimal divided = bdValue.divide(bdRoundTo, 0, RoundingMode.HALF_UP);
        BigDecimal result = divided.multiply(bdRoundTo);

        return result.doubleValue();
    }
}
