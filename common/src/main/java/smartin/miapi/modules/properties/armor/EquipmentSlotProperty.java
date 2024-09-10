package smartin.miapi.modules.properties.armor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;


/**
 * This property allows to dynamically set teh preferred EquipmentSlot
 * @header Equipment Slot Property
 * @description_start
 * It is recommneded to use the correct Item as well as this for better mod compatibility
 * @desciption_end
 * @path /data_types/properties/armor/equipment_slot
 * @data equipment_slot:the preferred Equipment Slot Group
 */
public class EquipmentSlotProperty extends CodecProperty<EquipmentSlotGroup> {
    public static final ResourceLocation KEY = Miapi.id("equipment_slot");
    public static EquipmentSlotProperty property;

    public EquipmentSlotProperty() {
        super(EquipmentSlotGroup.CODEC);
        property = this;
    }

    /**
     * Should this use the Cache?
     *
     * @param stack the Stack in question
     * @return the slot
     */
    @Nullable
    public static EquipmentSlotGroup getSlot(ItemStack stack) {
        return property.getData(stack).orElse(EquipmentSlotGroup.ANY);
    }

    @Override
    public EquipmentSlotGroup merge(EquipmentSlotGroup left, EquipmentSlotGroup right, MergeType mergeType) {
        if(mergeType.equals(MergeType.EXTEND)){
            return left;
        }
        return right;
    }
}
