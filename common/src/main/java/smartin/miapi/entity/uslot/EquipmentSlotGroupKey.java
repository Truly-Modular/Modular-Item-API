package smartin.miapi.entity.uslot;

import net.minecraft.world.entity.EquipmentSlotGroup;

/**
 * wrapper for EquipmentSlotGroups
 */
public record EquipmentSlotGroupKey(EquipmentSlotGroup equipmentSlot) implements UEquipmentSlot {
    @Override
    public String getString() {
        return equipmentSlot.name();
    }

    @Override
    public boolean matches(UEquipmentSlot slot) {
        return slot instanceof EquipmentSlotKey key && equipmentSlot.test(key.equipmentSlot()) ||
               slot instanceof EquipmentSlotGroupKey groupKey && groupKey.equipmentSlot().equals(this.equipmentSlot());
    }
}
