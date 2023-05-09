package smartin.miapi.attributes;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.ModularItem;

import java.util.Collection;

public class AttributeRegistry {
    public static final String ITEM_DURABILITY_ID = Miapi.MOD_ID + "durability";
    public static final EntityAttribute ITEM_DURABILITY = register(ITEM_DURABILITY_ID, (new ClampedEntityAttribute("attribute.name.miapi.durability", 300.0, 1.0, 16777216)).setTracked(true));

    public AttributeRegistry() {

    }

    private static EntityAttribute register(String id, EntityAttribute attribute) {
        return (EntityAttribute) Registry.register(Registry.ATTRIBUTE, id, attribute);
    }

    public static double getAttribute(ItemStack stack, EntityAttribute attribute,EquipmentSlot slot) {
        Collection<EntityAttributeModifier> attributes = stack.getAttributeModifiers(slot).get(attribute);
        Multimap<EntityAttribute, EntityAttributeModifier> map = HashMultimap.create();
        attributes.forEach(attributeModifier -> {
            map.put(attribute, attributeModifier);
        });

        DefaultAttributeContainer container = DefaultAttributeContainer.builder().add(attribute).build();

        AttributeContainer container1 = new AttributeContainer(container);

        container1.addTemporaryModifiers(map);

        return container1.getValue(attribute);
    }
}
