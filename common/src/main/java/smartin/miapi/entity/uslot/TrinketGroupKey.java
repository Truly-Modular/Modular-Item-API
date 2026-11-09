package smartin.miapi.entity.uslot;

import java.util.List;
import java.util.Optional;

public record TrinketGroupKey(String groupName) implements UEquipmentSlot {
    static List<String> prefixes = List.of("trinket.", "nucleus.", "accessories.", "curio.");

    static Optional<UEquipmentSlot> resolve(String input) {
        for (String prefix : prefixes) {
            if (input.startsWith(prefix)) {
                return Optional.of(new TrinketGroupKey(input.substring(prefix.length())));
            }
        }
        return Optional.empty();
    }

    @Override
    public String getString() {
        return "trinket." + groupName;
    }

    @Override
    public boolean matches(UEquipmentSlot slot) {
        return slot instanceof TrinketGroupKey key && (groupName.equals(key.groupName()) || key.groupName.equals("any"));
    }
}
