package smartin.miapi.item.modular.items.forge;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import smartin.miapi.item.modular.items.ModularElytraItem;
import smartin.miapi.modules.properties.AttributeProperty;
import top.theillusivec4.caelus.api.CaelusApi;

import java.util.UUID;

public class ModularElytraItemImpl extends ModularElytraItem {
    private static UUID uuid = UUID.randomUUID();

    public ModularElytraItemImpl() {
        super(new Settings().maxCount(1).fireproof());
    }

    public static ModularElytraItem getInstance() {
        AttributeProperty.attributeTransformers.add((map, itemstack) -> {
            if (itemstack.getItem() instanceof ModularElytraItem) {
                //TODO: use caelus for elytra
                map.put(
                        CaelusApi.getInstance().getFlightAttribute(),
                        new AttributeProperty.EntityAttributeModifierHolder(
                                new EntityAttributeModifier(
                                        uuid,
                                        "miapi_elytra",
                                        1,
                                        EntityAttributeModifier.Operation.ADDITION), EquipmentSlot.CHEST, false, EntityAttributeModifier.Operation.ADDITION));
            }
            return map;
        });
        return new ModularElytraItemImpl();
    }
}
