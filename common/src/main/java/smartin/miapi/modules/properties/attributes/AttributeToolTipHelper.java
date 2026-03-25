package smartin.miapi.modules.properties.attributes;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import smartin.miapi.modules.properties.LoreProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AttributeToolTipHelper {
    public static List<String> customSlots = new ArrayList<>();

    public static void addToolTip(String type){
        customSlots.add(type);
        LoreProperty.loreSuppliers.add((stack, tooltip, context, tooltipType) -> AttributeProperty.property.getData(stack).ifPresent(resourceLocationMapMap -> {
            AtomicBoolean addedHeader = new AtomicBoolean(false);
            AttributeUtil.AttributeContext ctx = new AttributeUtil.AttributeContext();
            ctx.map = resourceLocationMapMap;
            AttributeUtil.ITEM_ATTRIBUTE_ADJUST.invoker().adjust(ctx, stack);
            var idMap = ctx.map;
            idMap.forEach((attributeID, map) -> {
                Attribute attribute = AttributeProperty.findAttribute(attributeID);
                if (attribute == null) return;
                map.forEach((operation, slotMap) -> {
                    slotMap.forEach((group, resolvable) -> {
                        if (!type.equals(group.raw)) return;
                        double base = attribute.getDefaultValue();
                        double value = resolvable.evaluate(base).orElse(base) - base;

                        if (value == 0) return;

                        // header once
                        if (!addedHeader.get()) {
                            addedHeader.set(true);
                            tooltip.add(Component.empty());
                            tooltip.add(
                                    Component.translatable("item.miapi.modifiers."+type)
                                            .withStyle(ChatFormatting.GRAY)
                            );
                        }

                        tooltip.add(createModifierLine(attribute, value, operation));
                    });
                });
            });
        }));
    }

    private static Component createModifierLine(
            Attribute attribute,
            double value,
            AttributeModifier.Operation operation
    ) {
        boolean positive = value > 0;

        double displayValue = switch (operation) {
            case ADD_MULTIPLIED_BASE, ADD_MULTIPLIED_TOTAL -> value * 100.0;
            default -> value;
        };

        Component attributeName = Component.translatable(attribute.getDescriptionId());

        if (positive) {
            return Component.translatable(
                    "attribute.modifier.plus." + operation.id(),
                    ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(displayValue),
                    attributeName
            ).withStyle(ChatFormatting.BLUE);
        } else {
            return Component.translatable(
                    "attribute.modifier.take." + operation.id(),
                    ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(-displayValue),
                    attributeName
            ).withStyle(ChatFormatting.RED);
        }
    }
}
