package smartin.miapi.modules.properties;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AttributeSplitProperty implements ModuleProperty {

    public AttributeSplitProperty() {
        AttributeProperty.attributeTransformers.add(new AttributeProperty.AttributeTransformer() {
            @Override
            public Multimap<EntityAttribute, AttributeProperty.EntityAttributeModifierHolder> transform(Multimap<EntityAttribute, AttributeProperty.EntityAttributeModifierHolder> map, ItemStack itemstack) {
                map = ArrayListMultimap.create(map);
                Map<EntityAttribute, Map<EntityAttribute, Float>> replaceMap = getMap();
                for (Map.Entry<EntityAttribute, Map<EntityAttribute, Float>> entry : replaceMap.entrySet()) {
                    if (map.containsKey(entry.getKey())) {
                        Collection<AttributeProperty.EntityAttributeModifierHolder> list = map.get(entry.getKey());

                        list.stream().filter(attributeEntry -> {
                           return attributeEntry.mergeTo().equals(EntityAttributeModifier.Operation.ADDITION);
                        });

                    }
                }

                return map;
            }
        });
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }

    public Map<EntityAttribute, Map<EntityAttribute, Float>> getMap() {
        Map<EntityAttribute, Map<EntityAttribute, Float>> map = new HashMap<>();
        return map;
    }
}
