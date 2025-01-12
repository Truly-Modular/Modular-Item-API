package smartin.miapi.modules.properties.slot;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.registries.RegistryInventory;

import java.util.*;

/**
 * This property defines which module slots a specific module is allowed to occupy.
 * It allows restricting or specifying compatible slots for a given module.
 *
 * @header Allowed Slots Property
 * @path /data_types/properties/slot/allowed_in_slots
 * @description_start
 * The Allowed Slots Property controls where a module can be placed by defining which slot types it is compatible with.
 * A module can specify multiple slots by their IDs, ensuring that only certain modules can fit into designated slots
 * when assembling items. This helps define logical compatibility between modules and item slots.
 * @description_end
 * @data allowed_in_slots: A list of strings representing the slot IDs where the module can be placed.
 * @data `slotId`: (required) The ID of the slot that allows this module.
 */

public class AllowedSlots extends CodecProperty<List<String>> {
    public static final ResourceLocation KEY = Miapi.id("allowed_in_slots");
    public static Codec<List<String>> CODEC = Codec.list(Codec.STRING);
    static HashMap<String, Set<ItemModule>> allowedInMap = new HashMap<>();
    public static AllowedSlots property;

    public AllowedSlots() {
        super(CODEC);
        RegistryInventory.modules.addCallback(itemModule -> {
            getAllowedSlots(itemModule).forEach(slot -> {
                if (allowedInMap.containsKey(slot)) {
                    allowedInMap.get(slot).add(itemModule);
                } else {
                    Set<ItemModule> list = new HashSet<>();
                    list.add(itemModule);
                    allowedInMap.put(slot, list);
                }
            });
        });
        ReloadEvents.START.subscribe((isClient, registryAccess) -> {
            allowedInMap.clear();
        });
        property = this;
    }

    /**
     * Retrieves the allowed Submodules of a module
     *
     * @param module the module in question
     * @return List of slotIds
     */
    public static List<String> getAllowedSlots(ItemModule module) {
        return property.getData(module).orElse(new ArrayList<>());
    }

    /**
     * Retrieves the allowed Submodules of a module
     *
     * @param module the module in question
     * @return List of slotIds
     */
    public static List<String> getAllowedSlots(ModuleInstance module) {
        return property.getData(module).orElse(new ArrayList<>());
    }

    /**
     * retrieves all {@link ItemModule} that are allowed in a slot
     *
     * @param slot the slot in question
     * @return list of allowed {@link ItemModule}
     */
    public static List<ItemModule> allowedIn(SlotProperty.ModuleSlot slot) {
        if (slot == null) return new ArrayList<>();
        List<ItemModule> allowedModules = new ArrayList<>();
        slot.allowed.forEach(allowedKey -> {
            if (allowedInMap.containsKey(allowedKey)) {
                allowedModules.addAll(allowedInMap.get(allowedKey));
            }
        });
        return allowedModules;
    }

    @Override
    public List<String> merge(List<String> left, List<String> right, MergeType mergeType) {
        return MergeAble.mergeList(left, right, mergeType);
    }
}
