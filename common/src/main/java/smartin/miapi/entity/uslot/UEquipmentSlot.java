package smartin.miapi.entity.uslot;

import com.redpxnda.nucleus.trinket.NucleusTrinket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.registries.MiapiRegistry;

import java.util.Optional;
import java.util.function.Consumer;

public interface UEquipmentSlot {
    MiapiRegistry<SlotKeyResolver> RESOLVERS = createResolvers();
    MiapiRegistry<SlotProvider> PROVIDERS = createProviders();

    private static MiapiRegistry<SlotKeyResolver> createResolvers() {
        MiapiRegistry<SlotKeyResolver> registry = MiapiRegistry.getInstance(SlotKeyResolver.class);

        registry.register("equipment_slot", input ->
                Optional.ofNullable(EquipmentSlot.CODEC.byName(input))
                        .map(slot -> (UEquipmentSlot) new EquipmentSlotKey(slot)));

        registry.register("equipment_slot_group", input -> {
            try {
                return Optional.of(new EquipmentSlotGroupKey(EquipmentSlotGroup.valueOf(input)));
            } catch (IllegalArgumentException exception) {
                return Optional.empty();
            }
        });

        registry.register("trinket_group", TrinketGroupKey::resolve);

        return registry;
    }

    private static MiapiRegistry<SlotProvider> createProviders() {
        MiapiRegistry<SlotProvider> registry = MiapiRegistry.getInstance(SlotProvider.class);
        registry.register("equipment", UEquipmentSlot::scanEquipment);
        registry.register("trinkets", UEquipmentSlot::scanTrinkets);
        return registry;
    }

    private static void scanEquipment(LivingEntity entity, Consumer<EquipedItem> items) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack itemStack = entity.getItemBySlot(slot);
            if (!itemStack.isEmpty()) {
                items.accept(new EquipedItem(itemStack, new EquipmentSlotKey(slot)));
            }
        }
    }

    private static void scanTrinkets(LivingEntity entity, Consumer<EquipedItem> items) {
        NucleusTrinket.getItems(entity).forEach((slot, itemStack) -> {
            if (!itemStack.isEmpty()) {
                items.accept(new EquipedItem(itemStack, new TrinketGroupKey(slot.getSlotId())));
            }
        });
    }

    static UEquipmentSlot resolve(String input) {
        for (SlotKeyResolver resolver : RESOLVERS.getFlatMap().values()) {
            Optional<UEquipmentSlot> key = resolver.get(input);
            if (key.isPresent()) {
                return key.get();
            }
        }
        return new StringKey(input);
    }

    String getString();

    boolean matches(UEquipmentSlot slot);

    default void findItems(LivingEntity entity, Consumer<EquipedItem> items) {
        SlotSelection.scanItems(entity, item -> {
            if (matches(item.slot())) {
                items.accept(item);
            }
        });
    }

    record EquipedItem(ItemStack itemStack, UEquipmentSlot slot) {}

    interface SlotKeyResolver {
        Optional<UEquipmentSlot> get(String input);
    }

    /**
     * these should probably be implmented in a way to allow for lazy caching for repeated lookups.
     * Passive Potion Effect and similar might want to scan on a fixed scedule, so keeping the EquipedItems alive
     * is a good idea.
     */
    interface SlotProvider {
        void findItems(LivingEntity entity, Consumer<EquipedItem> items);
    }
}