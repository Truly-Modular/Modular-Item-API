package smartin.miapi.item.modular.properties;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.client.gui.crafting.moduleCrafter.ModuleCrafter;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.Transform;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class SlotProperty implements ModuleProperty {

    public static final String key = "slots";
    //TODO:figure out a consistent way for Slots to work

    public static Transform getTransform(ItemModule.ModuleInstance instance){
        ItemModule.ModuleInstance current = instance;
        Transform mergedTransform = getLocalTransform(current);
        while(current.parent!=null){
            current = current.parent;
            mergedTransform = Transform.merge(getLocalTransform(current),mergedTransform);
        }
        return mergedTransform;
    }

    public static Map<Integer,ModuleSlot> getSlots(ItemModule.ModuleInstance instance){
        JsonElement data = instance.module.getProperties().get(key);
        if(data!=null){
            Gson gson = new Gson();
            Type type = new TypeToken<Map<Integer, ModuleSlot>>(){}.getType();
            Map<Integer, ModuleSlot> slots = gson.fromJson(data,type);
            //need to set Inslot as well here
            slots.forEach((number,slot)->{
                slot.inSlot = instance.subModules.get(number);
                slot.parent = instance;
                slot.id = number;
            });
            return slots;
        }
        return new HashMap<>();
    }

    public static Integer getSlotNumberIn(ItemModule.ModuleInstance instance){
        if(instance.parent!=null){
            Map<Integer,ModuleSlot> slots = getSlots(instance.parent);
            AtomicReference<Integer> id = new AtomicReference<>(0);
            slots.forEach((number,moduleSlot)->{
                if(moduleSlot.inSlot==instance){
                    id.set(number);
                }
            });
            return id.get();
        }
        return 0;
    }
    public static Transform getLocalTransform(ItemModule.ModuleInstance instance){
        JsonElement test = instance.module.getProperties().get(key);
        if(test!=null){
            Gson gson = new Gson();
            //TODO:
            ModuleSlot slot = getSlotIn(instance);
            if(slot!=null){
                return slot.transform;
            }
            else{
                //Miapi.LOGGER.warn("No Slot Found for ModuleInstance");
            }
        }
        return Transform.IDENTITY;
    }

    @Nullable
    public static ModuleSlot getSlotIn(ItemModule.ModuleInstance instance){
        if(instance.parent!=null){
            Map<Integer,ModuleSlot> slots = getSlots(instance.parent);
            return slots.values().stream().filter(moduleSlot -> {
                if(moduleSlot.inSlot==null) return false;
                return moduleSlot.inSlot.equals(instance);
            }).findFirst().orElse(null);
        }
        return null;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<Integer, ModuleSlot>>(){}.getType();
        Map<Integer, ModuleSlot> map = gson.fromJson(data,type);
        return true;
    }

    public static class ModuleSlot{
        public ModuleSlot(List<String> allowedList){
            this.allowed = allowedList;
            id = 0;
        }
        public List<String> allowed = new ArrayList<>();
        public Transform transform = Transform.IDENTITY;
        @Nullable
        public ItemModule.ModuleInstance inSlot;
        public ItemModule.ModuleInstance parent;
        public int id;
    }
}
