package smartin.miapi.modules.properties.projectile.stat.bow;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.projectile.stat.BowStatProperty;

import java.text.DecimalFormat;

/**
 * @header DrawTimeProperty
 * @path /data_types/properties/projectile/draw_time
 * @description_start This Property manages the Draw time of Modular Bows and Crossbows in seconds.
 * This is a Double Resolvable, so different operations are possible
 * @description_end
 * @data value:the drawtime in seconds.
 */
public class BowDrawTimeProperty extends BowStatProperty {
    public static final ResourceLocation KEY = Miapi.id("draw_time");
    public static BowDrawTimeProperty property;
    public static DecimalFormat FORMAT = new DecimalFormat("##.##");

    public BowDrawTimeProperty() {
        super(KEY, FORMAT, 0.05);
        property = this;
        super.lowerBound = 0.99;
        super.upperBound = 1.01;
        LoreProperty.loreSuppliers.add((itemStack, tooltip, context, tooltipType) -> {
            if (
                    BowAccuracyProperty.property.isPresent(itemStack) ||
                    BowDrawTimeProperty.property.isPresent(itemStack) ||
                    BowSpeedProperty.property.isPresent(itemStack)
            ) {

                tooltip.add(
                        Component.translatable(isExtendedTooltip() ? "miapi.lore.bow.long" : "miapi.lore.bow.short",
                                getToolTipValue(itemStack),
                                BowSpeedProperty.property.getToolTipValue(itemStack),
                                BowAccuracyProperty.property.getToolTipValue(itemStack))
                );
            }
        });
    }

    public boolean isPercentStat() {
        return false;
    }
}
