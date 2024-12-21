package smartin.miapi.modules.properties.onHit;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dev.architectury.event.EventResult;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.util.DoubleProperty;

import java.util.Optional;
import java.util.WeakHashMap;

/**
 * This property allows for armor penetration, so weapons can ignore some armor.
 *
 * @header Armor Penetration Property
 * @path /data_types/properties/on_hit/armor_pen
 * @description_start
 * The Armor Penetration Property is used to reduce the effectiveness of armor on entities when hit by items with this property.
 * It adjusts the damage calculation by applying a temporary attribute modifier to decrease the armor value of the target. This allows weapons to bypass a portion of the target's armor.
 * The value specified for armor penetration is treated as a percentage reduction of the armor's effectiveness.
 * @description_end
 * @data value: The percentage of armor penetration applied. A value of 100 means complete armor penetration, while a lower value means less reduction.
 */

public class ArmorPenProperty extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("armor_pen");
    public static ArmorPenProperty property;
    private static final WeakHashMap<LivingEntity, Multimap<Holder<Attribute>, AttributeModifier>> cache = new WeakHashMap<>();

    public ArmorPenProperty() {
        super(KEY);
        property = this;
        MiapiEvents.LIVING_HURT.register((event -> {
            if (event.damageSource.getEntity() instanceof LivingEntity attacker) {
                ItemStack itemStack = event.getCausingItemStack();
                Optional<Double> optionalDouble = getValue(itemStack);
                if (optionalDouble.isPresent()) {
                    double value = optionalDouble.get() / 100;
                    Multimap<Holder<Attribute>, AttributeModifier> multimap = ArrayListMultimap.create();
                    multimap.put(Attributes.ARMOR, new AttributeModifier(Miapi.id("temp_armor_pen"), (- 1 + value), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                    cache.put(event.defender, multimap);
                    event.defender.getAttributes().addTransientAttributeModifiers(multimap);
                }
            }
            return EventResult.pass();
        }));

        MiapiEvents.LIVING_HURT_AFTER.register((event -> {
            if (cache.containsKey(event.defender)) {
                event.defender.getAttributes().removeAttributeModifiers(cache.get(event.defender));
            }
            return EventResult.pass();
        }));
    }
}
