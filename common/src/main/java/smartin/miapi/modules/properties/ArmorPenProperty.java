package smartin.miapi.modules.properties;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dev.architectury.event.EventResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import smartin.miapi.events.Event;
import smartin.miapi.modules.properties.util.SimpleDoubleProperty;

import java.util.HashMap;
import java.util.Map;

public class ArmorPenProperty extends SimpleDoubleProperty {
    public static final String KEY = "armor_pen";
    public static ArmorPenProperty property;
    private static Map<LivingEntity, Multimap<EntityAttribute, EntityAttributeModifier>> cache = new HashMap<>();

    public ArmorPenProperty() {
        super(KEY);
        property = this;
        Event.LIVING_HURT.register((event -> {
            if (event.damageSource.getAttacker() instanceof LivingEntity attacker) {
                ItemStack itemStack = attacker.getMainHandStack();
                if (property.hasValue(itemStack)) {
                    double value = -valueRemap(property.getValue(itemStack))/100;
                    Multimap<EntityAttribute, EntityAttributeModifier> multimap = ArrayListMultimap.create();
                    multimap.put(EntityAttributes.GENERIC_ARMOR, new EntityAttributeModifier("tempArmorPen", value, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    cache.put(event.livingEntity, multimap);
                    event.livingEntity.getAttributes().addTemporaryModifiers(multimap);
                }
            }
            return EventResult.pass();
        }));

        Event.LIVING_HURT_AFTER.register((event -> {
            if (cache.containsKey(event.livingEntity)) {
                event.livingEntity.getAttributes().removeModifiers(cache.get(event.livingEntity));
            }
            return EventResult.pass();
        }));
    }

    public static double valueRemap(double value) {
        return ((200) / (1 + Math.exp(-(value) / 120)) - 100);
    }
}
