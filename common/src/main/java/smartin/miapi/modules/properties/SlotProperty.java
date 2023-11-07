package smartin.miapi.modules.properties;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.item.modular.TransformMap;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The SlotProperty, this allows Modules to define submodule Slots
 */
public class SlotProperty implements ModuleProperty {

    public static final String KEY = "slots";

    public static SlotProperty getInstance() {
        return (SlotProperty) RegistryInventory.moduleProperties.get(KEY);
    }

    @Environment(EnvType.CLIENT)
    public static Transform getTransform(ItemModule.ModuleInstance instance) {
        ModuleSlot slot = getSlotIn(instance);
        if (slot == null) return Transform.IDENTITY;
        ItemModule.ModuleInstance current = instance;
        Transform merged = Transform.IDENTITY;
        while (current != null) {
            merged = Transform.merge(getLocalTransform(current), merged);
            current = current.parent;
        }
        return getTransform(slot);
    }

    @Environment(EnvType.CLIENT)
    public static Transform getTransform(ModuleSlot moduleSlot) {
        ItemModule.ModuleInstance current = moduleSlot.parent;
        Transform mergedTransform = Transform.IDENTITY;
        while (current != null) {
            mergedTransform = Transform.merge(getLocalTransform(current), mergedTransform);
            current = current.parent;
        }
        mergedTransform = Transform.merge(mergedTransform, moduleSlot.transform);
        return mergedTransform;
    }

    @Environment(EnvType.CLIENT)
    public static Transform getTransform(ModuleSlot moduleSlot, String id) {
        return getTransformStack(moduleSlot).get(id);
    }

    @Environment(EnvType.CLIENT)
    public static TransformMap getTransformStack(ItemModule.ModuleInstance instance) {
        ModuleSlot slot = getSlotIn(instance);
        if (slot == null) return new TransformMap();
        return getTransformStack(slot);
    }

    @Environment(EnvType.CLIENT)
    public static TransformMap getTransformStack(ModuleSlot moduleSlot) {
        if (moduleSlot == null) {
            return new TransformMap();
        }
        ItemModule.ModuleInstance current = moduleSlot.parent;
        TransformMap mergedTransform = new TransformMap();
        while (current != null) {
            TransformMap stack = getLocalTransformStack(current);
            if (mergedTransform.primary == null && stack.primary != null) {
                mergedTransform.set(stack.primary, mergedTransform.get(null));
                mergedTransform.set(null, Transform.IDENTITY);
            }
            mergedTransform = TransformMap.merge(getLocalTransformStack(current),mergedTransform);
            current = current.parent;
        }
        mergedTransform = TransformMap.merge(moduleSlot.getTransformStack(),mergedTransform);
        return mergedTransform;
    }

    public static Map<Integer, ModuleSlot> getSlots(ItemModule.ModuleInstance instance) {
        ModuleProperty property = RegistryInventory.moduleProperties.get(KEY);
        JsonElement data = instance.getProperties().get(property);
        if (data != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<Integer, ModuleSlot>>() {
            }.getType();
            Map<Integer, ModuleSlot> slots = gson.fromJson(data, type);
            //need to set Inslot as well here
            slots.forEach((number, slot) -> {
                slot.inSlot = instance.subModules.get(number);
                slot.parent = instance;
                slot.id = number;
            });
            return slots;
        }
        return new HashMap<>();
    }

    public static Integer getSlotNumberIn(ItemModule.ModuleInstance instance) {
        if (instance.parent != null) {
            Map<Integer, ModuleSlot> slots = getSlots(instance.parent);
            AtomicReference<Integer> id = new AtomicReference<>(0);
            slots.forEach((number, moduleSlot) -> {
                if (moduleSlot.inSlot == instance) {
                    id.set(number);
                }
            });
            return id.get();
        }
        return 0;
    }

    @Environment(EnvType.CLIENT)
    public static Transform getLocalTransform(ItemModule.ModuleInstance instance) {
        ModuleProperty property = RegistryInventory.moduleProperties.get(KEY);
        JsonElement test = instance.getProperties().get(property);
        if (test != null) {
            ModuleSlot slot = getSlotIn(instance);
            if (slot != null) {
                return slot.transform;
            }
        }
        return Transform.IDENTITY;
    }

    @Environment(EnvType.CLIENT)
    public static TransformMap getLocalTransformStack(ItemModule.ModuleInstance instance) {
        ModuleProperty property = RegistryInventory.moduleProperties.get(KEY);
        JsonElement test = instance.getProperties().get(property);
        if (test != null) {
            ModuleSlot slot = getSlotIn(instance);
            if (slot != null) {
                Transform transform = slot.transform;
                TransformMap stack = new TransformMap();
                stack.add(transform);
                stack.primary = transform.origin;
                return stack;
            }
        }
        return new TransformMap();
    }

    @Nullable
    public static ModuleSlot getSlotIn(ItemModule.ModuleInstance instance) {
        if (instance != null && instance.parent != null) {
            Map<Integer, ModuleSlot> slots = getSlots(instance.parent);
            ModuleSlot slot = slots.values().stream().filter(moduleSlot -> {
                if (moduleSlot.inSlot == null) return false;
                return moduleSlot.inSlot.equals(instance);
            }).findFirst().orElse(null);
            if (slot != null && slot.transform.origin != null && slot.transform.origin.equals("")) {
                slot.transform.origin = null;
            }
            return slot;
        }
        return null;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<Integer, ModuleSlot>>() {
        }.getType();
        gson.fromJson(data, type);
        return true;
    }

    public static class ModuleSlot {
        public ModuleSlot(List<String> allowedList) {
            this.allowed = allowedList;
            id = 0;
        }

        public List<String> allowed;
        //@Environment(EnvType.CLIENT) // POTENTIAL BEHAVIOR CHANGE (Panda's fix to #11): removed @environment notation to fix NoSuchFieldError
        public Transform transform = Transform.IDENTITY;
        @Nullable
        public ItemModule.ModuleInstance inSlot;
        public ItemModule.ModuleInstance parent;
        public int id;

        public boolean allowedIn(ItemModule.ModuleInstance instance) {
            List<String> allowedSlots = AllowedSlots.getAllowedSlots(instance.module);
            for (String key : allowed) {
                if (allowedSlots.contains(key)) {
                    return true;
                }
            }
            return false;
        }

        @Environment(EnvType.CLIENT)
        public TransformMap getTransformStack() {
            TransformMap stack = new TransformMap();
            stack.add(transform);
            return stack;
        }

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
