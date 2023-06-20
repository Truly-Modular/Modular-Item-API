package smartin.miapi.attributes;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.architectury.event.EventResult;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;
import smartin.miapi.events.Event;
import smartin.miapi.Miapi;

import java.util.Collection;


public class AttributeRegistry {
    public static final EntityAttribute ITEM_DURABILITY = register(Miapi.MOD_ID + ":generic.durability", (new ClampedEntityAttribute("miapi.attribute.name.durability", 300.0, 1.0, 16777216)).setTracked(true));
    public static final EntityAttribute DAMAGE_RESISTANCE = register(Miapi.MOD_ID + ":generic.resistance", (new ClampedEntityAttribute("miapi.attribute.name.resistance", 0.0, 0.0, 100)).setTracked(true));
    public static final EntityAttribute REACH = register(Miapi.MOD_ID + ":generic.reach", (new ClampedEntityAttribute("miapi.attribute.name.reach", 0.0, -1024.0, 1024.0)).setTracked(true));
    public static final EntityAttribute ATTACK_RANGE = register(Miapi.MOD_ID + ":generic.attack_range", (new ClampedEntityAttribute("miapi.attribute.name.attack_range", 0.0, -1024.0, 1024.0)).setTracked(true));

    public static final EntityAttribute MINING_SPEED_PICKAXE = register(Miapi.MOD_ID + ":generic.mining_speed.pickaxe", (new ClampedEntityAttribute("miapi.attribute.name.mining_speed.pickaxe", 1.0, 1.0, 1024.0)).setTracked(true));
    public static final EntityAttribute MINING_SPEED_AXE = register(Miapi.MOD_ID + ":generic.mining_speed.axe", (new ClampedEntityAttribute("miapi.attribute.name.mining_speed.axe", 1.0, 1.0, 1024.0)).setTracked(true));
    public static final EntityAttribute MINING_SPEED_SHOVEL = register(Miapi.MOD_ID + ":generic.mining_speed.shovel", (new ClampedEntityAttribute("miapi.attribute.name.mining_speed.shovel", 1.0, 1.0, 1024.0)).setTracked(true));
    public static final EntityAttribute MINING_SPEED_HOE = register(Miapi.MOD_ID + ":generic.mining_speed.hoe", (new ClampedEntityAttribute("miapi.attribute.name.mining_speed.hoe", 1.0, 1.0, 1024.0)).setTracked(true));




    public static void setup() {
        Event.LIVING_HURT.register((livingHurtEvent -> {
            if (livingHurtEvent.livingEntity.getAttributes().hasAttribute(DAMAGE_RESISTANCE)) {
                livingHurtEvent.amount = (float) (livingHurtEvent.amount * (100 - livingHurtEvent.livingEntity.getAttributeValue(DAMAGE_RESISTANCE))/100);
            }
            return EventResult.pass();
        }));
    }

    private static EntityAttribute register(String id, EntityAttribute attribute) {
        return Registry.register(Registry.ATTRIBUTE, id, attribute);
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
