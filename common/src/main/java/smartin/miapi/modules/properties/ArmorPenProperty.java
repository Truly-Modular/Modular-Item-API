package smartin.miapi.modules.properties;

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

import javax.swing.text.html.Option;
import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * This property allows for armor penetration, so weapons can igonre some armor
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
}
