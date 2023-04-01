package smartin.miapi.item.modular.properties.crafting;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import smartin.miapi.datapack.ReloadEvent;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.properties.ModuleProperty;
import smartin.miapi.item.modular.properties.SlotProperty;

import java.lang.reflect.Type;
import java.util.*;

public class AllowedSlots implements ModuleProperty {
    public static String key = "allowedInSlots";
    static HashMap<String, Set<ItemModule>> allowedInMap = new HashMap<>();

    public AllowedSlots(){
        ModularItem.moduleRegistry.addCallback(itemModule -> {
            getAllowedSlots(itemModule).forEach(slot -> {
                if(allowedInMap.containsKey(slot)){
                    allowedInMap.get(slot).add(itemModule);
                }
                else{
                    Set<ItemModule> list = new HashSet<>();
                    list.add(itemModule);
                    allowedInMap.put(slot,list);
                }
            });
        });
        ReloadEvent.subscribeStart(isClient ->{
            allowedInMap.clear();
        });
    }

    public static List<String> getAllowedSlots(ItemModule module){
        List<String> slots = new ArrayList<>();
        JsonElement data = module.getProperties().get(key);
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>(){}.getType();
        return gson.fromJson(data,type);
    }

    public static List<ItemModule> allowedIn(SlotProperty.ModuleSlot slot){
        if(slot==null) return new ArrayList<>();
        List<ItemModule> allowedModules = new ArrayList<>();
        slot.allowed.forEach(allowedKey->{
            if(allowedInMap.containsKey(allowedKey)){
                allowedModules.addAll(allowedInMap.get(allowedKey));
            }
        });
        return allowedModules;
    }


    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>(){}.getType();
        gson.fromJson(data,type);
        return true;
    }
}
