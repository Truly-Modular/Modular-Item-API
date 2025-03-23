package smartin.miapi.modules.properties;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * This Property is meant to control what is allowed in the moduleSlots of an module
 */
public class AllowedSlots implements ModuleProperty {
    public static final String KEY = "allowedInSlots";

    public AllowedSlots() {
    }

    /**
     * Retrieves the allowed Submodules of a module
     *
     * @param module the module in question
     * @return List of slotIds
     */
    public static List<String> getAllowedSlots(ItemModule module) {
        JsonElement data = module.getProperties().get(KEY);
        if(data==null){
            return List.of();
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>() {
        }.getType();
        return gson.fromJson(data, type);
    }

    /**
     * retrieves all {@link ItemModule} that are allowed in a slot
     *
     * @param slot the slot in question
     * @return list of allowed {@link ItemModule}
     */
    public static List<ItemModule> allowedIn(SlotProperty.ModuleSlot slot) {
        if (slot == null) return new ArrayList<>();
        return RegistryInventory.modules.getFlatMap().values().stream().filter(m -> slot.allowed.stream().anyMatch(s -> getAllowedSlots(m).contains(s))).toList();
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>() {
        }.getType();
        gson.fromJson(data, type);
        return true;
    }
}
