package smartin.miapi.attributes;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;
import smartin.miapi.Events.Event;
import smartin.miapi.Miapi;

import java.util.Collection;

public class AttributeRegistry {
    public static final String ITEM_DURABILITY_ID = Miapi.MOD_ID + "durability";
    public static final String DAMAGE_RESISTANCE_ID = Miapi.MOD_ID + "resistance";
    public static final EntityAttribute ITEM_DURABILITY = register(ITEM_DURABILITY_ID, (new ClampedEntityAttribute("attribute.name.miapi.durability", 300.0, 1.0, 16777216)).setTracked(true));
    public static final EntityAttribute DAMAGE_RESISTANCE = register(DAMAGE_RESISTANCE_ID, (new ClampedEntityAttribute("attribute.name.miapi.resistance", 0.0, 0.0, 100)).setTracked(true));


    public static void setup() {
        Event.LIVING_HURT.register((livingHurtEvent -> {
            if (livingHurtEvent.livingEntity.getAttributes().hasAttribute(DAMAGE_RESISTANCE)) {
                livingHurtEvent.amount = (float) (livingHurtEvent.amount * (100 - livingHurtEvent.livingEntity.getAttributeValue(DAMAGE_RESISTANCE))/100);
            }
            return EventResult.pass();
        }));
    }

    private static EntityAttribute register(String id, EntityAttribute attribute) {
        return (EntityAttribute) Registry.register(Registry.ATTRIBUTE, id, attribute);
    }

    public static double getAttribute(ItemStack stack, EntityAttribute attribute, EquipmentSlot slot, double defaultValue) {
        Collection<EntityAttributeModifier> attributes = stack.getAttributeModifiers(slot).get(attribute);
        Multimap<EntityAttribute, EntityAttributeModifier> map = HashMultimap.create();
        attributes.forEach(attributeModifier -> {
            map.put(attribute, attributeModifier);
        });

        DefaultAttributeContainer container = DefaultAttributeContainer.builder().add(attribute, defaultValue).build();

        AttributeContainer container1 = new AttributeContainer(container);

        container1.addTemporaryModifiers(map);

        return container1.getValue(attribute);
    }
}
