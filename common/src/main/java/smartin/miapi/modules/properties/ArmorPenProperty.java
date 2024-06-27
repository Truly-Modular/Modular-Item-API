package smartin.miapi.modules.properties;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dev.architectury.event.EventResult;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.util.DoubleProperty;

import java.util.Objects;
import java.util.WeakHashMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

/**
 * This property allows for armor penetration, so weapons can igonre some armor
 */
public class ArmorPenProperty extends DoubleProperty {
    public static final String KEY = "armor_pen";
    public static ArmorPenProperty property;
    private static WeakHashMap<LivingEntity, Multimap<Attribute, AttributeModifier>> cache = new WeakHashMap<>();

    public ArmorPenProperty() {
        super(KEY);
        property = this;
        MiapiEvents.LIVING_HURT.register((event -> {
            if (event.damageSource.getEntity() instanceof LivingEntity attacker) {
                ItemStack itemStack = event.getCausingItemStack();
                if (property.hasValue(itemStack)) {
                    double value = property.getValueSafe(itemStack) / 100;
                    Multimap<Attribute, AttributeModifier> multimap = ArrayListMultimap.create();
                    multimap.put(Attributes.ARMOR, new AttributeModifier("tempArmorPen", (- 1 + value), EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    cache.put(event.livingEntity, multimap);
                    event.livingEntity.getAttributes().addTransientAttributeModifiers(multimap);
                }
            }
            return EventResult.pass();
        }));

        MiapiEvents.LIVING_HURT_AFTER.register((event -> {
            if (cache.containsKey(event.livingEntity)) {
                event.livingEntity.getAttributes().removeAttributeModifiers(cache.get(event.livingEntity));
            }
            return EventResult.pass();
        }));
    }


    public static double valueRemap(double value) {
        return 100 - ((200) / (1 + Math.exp(-(value) / 120)) - 100);
    }

    @Override
    public Double getValue(ItemStack stack) {
        Double value = getValueRaw(stack);
        if (value != null) {
            return valueRemap(value);
        }
        return null;
    }

    @Override
    public double getValueSafe(ItemStack stack) {
        Double value = getValueRaw(stack);
        return valueRemap(Objects.requireNonNullElse(value, 0.0));
    }
}
