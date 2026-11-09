package smartin.miapi.entity.uslot;

import net.minecraft.world.entity.EquipmentSlot;

public record EquipmentSlotKey(EquipmentSlot equipmentSlot) implements UEquipmentSlot {
    @Override
    public String getString() {
        return equipmentSlot.getName();
    }

    @Override
    public boolean matches(UEquipmentSlot slot) {
        return slot instanceof EquipmentSlotKey key && (equipmentSlot == key.equipmentSlot());
    }
}
