package smartin.miapi.modules.properties;

import com.mojang.serialization.Codec;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.*;

/**
 * This Property is meant to control what is allowed in the moduleSlots of an module
 */
public class AllowedSlots extends CodecProperty<List<String>> {
    public static final String KEY = "allowedInSlots";
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
        ReloadEvents.START.subscribe(isClient -> {
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
        return ModuleProperty.mergeList(left, right, mergeType);
    }
}
