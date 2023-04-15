package smartin.miapi.item.modular.properties;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.Transform;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class SlotProperty implements ModuleProperty {

    public static final String key = "slots";

    public static SlotProperty getInstance(){
        return (SlotProperty) Miapi.modulePropertyRegistry.get(key);
    }

    public static Transform getTransform(ItemModule.ModuleInstance instance){
        ItemModule.ModuleInstance current = instance;
        Transform mergedTransform = getLocalTransform(current);
        while(current.parent!=null){
            current = current.parent;
            mergedTransform = Transform.merge(getLocalTransform(current),mergedTransform);
        }
        return mergedTransform;
    }

    public static Transform getTransform(ModuleSlot moduleSlot){
        ItemModule.ModuleInstance current = moduleSlot.parent;
        Transform mergedTransform = Transform.IDENTITY;
        while(current!=null){
            mergedTransform = Transform.merge(getLocalTransform(current),mergedTransform);
            current = current.parent;
        }
        mergedTransform = Transform.merge(mergedTransform,moduleSlot.transform);
        return mergedTransform;
    }

    public static Map<Integer,ModuleSlot> getSlots(ItemModule.ModuleInstance instance){
        ModuleProperty property = Miapi.modulePropertyRegistry.get(key);
        JsonElement data = instance.getProperties().get(property);
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
        ModuleProperty property = Miapi.modulePropertyRegistry.get(key);
        JsonElement test = instance.getProperties().get(property);
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

        @Override
        public boolean equals(Object object) {
            if (object instanceof ModuleSlot slot) {
                if (this.parent == null && slot.parent != null) {
                    return false;
                }
                if (this.parent != null && !this.parent.equals(slot.parent)) {
                    return false;
                }
                if (this.inSlot == null && slot.inSlot != null) {
                    return false;
                }
                if (this.inSlot != null && !this.inSlot.equals(slot.inSlot)) {
                    return false;
                }
                if (this.allowed == null && slot.allowed != null) {
                    return false;
                }
                if (this.allowed != null) {
                    List<String> sortedAllowed = new ArrayList<>(this.allowed);
                    Collections.sort(sortedAllowed);
                    List<String> sortedOtherAllowed = new ArrayList<>(slot.allowed);
                    Collections.sort(sortedOtherAllowed);
                    if (!sortedAllowed.equals(sortedOtherAllowed)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
    }
}