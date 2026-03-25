package smartin.miapi.modules.properties.trinket;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.redpxnda.nucleus.trinket.CommonSlotReference;
import com.redpxnda.nucleus.trinket.Trinket;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.properties.attributes.AttributeProperty;

public class ModularTrinket implements Trinket {

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
            CommonSlotReference ctx, ResourceLocation id, ItemStack stack) {
        Multimap<Holder<Attribute>, AttributeModifier> finishedMap = ArrayListMultimap.create();
        AttributeProperty.property.getData(stack).ifPresent(resourceLocationMapMap -> {
            resourceLocationMapMap.forEach((attributeID, map) -> {
                Attribute attribute = AttributeProperty.findAttribute(attributeID);
                Holder<Attribute> attributeHolder = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute);
                map.forEach((op, a) -> {
                    a.forEach((group, resolveable) -> {
                        if (group.raw.equals("trinket")) {
                            finishedMap.put(
                                    attributeHolder,
                                    new AttributeModifier(
                                            id,
                                            resolveable.getValue(),
                                            op));
                        }
                    });
                });
            });
        });
        return finishedMap;
    }
}
