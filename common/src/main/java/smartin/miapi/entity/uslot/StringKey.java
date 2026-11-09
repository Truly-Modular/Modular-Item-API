package smartin.miapi.entity.uslot;

/**
 * only here to capture typos in slots to not error out. maybe this shouldnt exist, but is prob. a good fallback.
 */
public record StringKey(String id) implements UEquipmentSlot {
    static UEquipmentSlot resolve(String input) {
        return UEquipmentSlot.resolve(input);
    }

    @Override
    public String getString() {
        return id;
    }

    @Override
    public boolean matches(UEquipmentSlot slot) {
        return false;
    }
}
