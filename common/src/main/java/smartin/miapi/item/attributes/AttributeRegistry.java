package smartin.miapi.item.attributes;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.util.registry.Registry;
import smartin.miapi.Miapi;

public class AttributeRegistry {
    public static final String ITEM_DURABILITY_ID = Miapi.MOD_ID+"durability";
    public static final EntityAttribute ITEM_DURABILITY = register(ITEM_DURABILITY_ID, (new ClampedEntityAttribute("attribute.name.miapi.durability", 300.0, 1.0, 16777216)).setTracked(true));

    public AttributeRegistry(){

    }

    private static EntityAttribute register(String id, EntityAttribute attribute) {
        return (EntityAttribute) Registry.register(Registry.ATTRIBUTE, id, attribute);
    }
}
