package smartin.miapi.item.modular;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import smartin.miapi.attributes.AttributeRegistry;

import java.util.Collection;

/**
 * Empty Interface to identify Modular Item
 */
public interface ModularItem{

    static int getDurability(ItemStack stack){
        if (stack.getItem() instanceof ModularItem) {
            Collection<EntityAttributeModifier> attributes = stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(AttributeRegistry.ITEM_DURABILITY);
            Multimap<EntityAttribute,EntityAttributeModifier> map = HashMultimap.create();
            attributes.forEach(attribute->{
                map.put(AttributeRegistry.ITEM_DURABILITY,attribute);
            });

            DefaultAttributeContainer container = DefaultAttributeContainer.builder().add(AttributeRegistry.ITEM_DURABILITY).build();

            AttributeContainer container1 = new AttributeContainer(container);

            container1.addTemporaryModifiers(map);

            return (int) container1.getValue(AttributeRegistry.ITEM_DURABILITY);
        }
        return stack.getMaxDamage();
    }
}
