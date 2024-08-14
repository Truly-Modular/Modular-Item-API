package smartin.miapi.modules.properties.slot;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.item.modular.TransformMap;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The SlotProperty, this allows Modules to define submodule Slots
 */
public class SlotProperty extends CodecProperty<Map<String, SlotProperty.ModuleSlot>> {
    public static Codec<Map<String, ModuleSlot>> CODEC = Codec.unboundedMap(Codec.STRING, AutoCodec.of(ModuleSlot.class).codec());

    public static final String KEY = "slots";

    public SlotProperty() {
        super(CODEC);
    }

    public static SlotProperty getInstance() {
        return (SlotProperty) RegistryInventory.moduleProperties.get(KEY);
    }

    @Environment(EnvType.CLIENT)
    public static Transform getTransform(ModuleSlot moduleSlot) {
        ModuleInstance current = moduleSlot.parent;
        Transform mergedTransform = Transform.IDENTITY;
        while (current != null) {
            mergedTransform = Transform.merge(getLocalTransform(current), mergedTransform);
            current = current.getParent();
        }
        mergedTransform = Transform.merge(mergedTransform, moduleSlot.transform);
        return mergedTransform;
    }

    @Environment(EnvType.CLIENT)
    public static TransformMap getTransformStack(ModuleInstance instance) {
        ModuleSlot slot = getSlotIn(instance);
        if (slot == null) return new TransformMap();
        return getTransformStack(slot);
    }

    @Environment(EnvType.CLIENT)
    public static TransformMap getTransformStack(ModuleSlot moduleSlot) {
        if (moduleSlot == null) {
            return new TransformMap();
        }
        ModuleInstance current = moduleSlot.parent;
        TransformMap mergedTransform = new TransformMap();
        while (current != null) {
            TransformMap stack = getLocalTransformStack(current);
            if (mergedTransform.primary == null && stack.primary != null) {
                mergedTransform.set(stack.primary, mergedTransform.get(null));
                mergedTransform.set(null, Transform.IDENTITY);
            }
            mergedTransform = TransformMap.merge(stack, mergedTransform);
            current = current.getParent();
        }
        mergedTransform = TransformMap.merge(moduleSlot.getTransformStack(), mergedTransform);
        return mergedTransform;
    }

    public static Map<String, ModuleSlot> getSlots(ModuleInstance instance) {
        return getInstance().getData(instance).orElse(new HashMap<>());
    }

    public static String getSlotID(ModuleInstance instance) {
        if (instance.getParent() != null) {
            Map<String, ModuleSlot> slots = getSlots(instance.getParent());
            AtomicReference<String> id = new AtomicReference<>("primary");
            slots.forEach((number, moduleSlot) -> {
                if (moduleSlot.inSlot != null && moduleSlot.inSlot.equals(instance)) {
                    id.set(number);
                }
            });
            return id.get();
        }
        return "primary";
    }

    @Environment(EnvType.CLIENT)
    public static Transform getLocalTransform(ModuleInstance instance) {
        ModuleSlot slot = getSlotIn(instance);
        if (slot != null) {
            return slot.transform;
        }
        return Transform.IDENTITY;
    }

    @Environment(EnvType.CLIENT)
    public static TransformMap getLocalTransformStack(ModuleInstance instance) {
        ModuleSlot slot = getSlotIn(instance);
        if (slot != null) {
            Transform transform = slot.transform;
            TransformMap stack = new TransformMap();
            stack.add(transform);
            stack.primary = transform.origin;
            return stack;
        }
        return new TransformMap();
    }

    @Nullable
    public static ModuleSlot getSlotIn(ModuleInstance instance) {
        if (instance != null && instance.getParent() != null) {
            Map<String, ModuleSlot> slots = getSlots(instance.getParent());
            ModuleSlot slot = slots.values().stream().filter(moduleSlot -> {
                if (moduleSlot.inSlot == null) return false;
                return moduleSlot.inSlot.equals(instance);
            }).findFirst().orElse(null);
            if (slot != null && slot.transform.origin != null && slot.transform.origin.isEmpty()) {
                slot.transform.origin = null;
            }
            if (slot != null && slot.translationKey == null) {
                slot.translationKey = "miapi.module.empty.name";
            }
            return slot;
        }
        return null;
    }

    public Map<String, ModuleSlot> decode(JsonElement element) {
        Map<String, ModuleSlot> map = super.decode(element);
        map.forEach((key, slot) -> {
            slot.id = key;
        });
        return map;
    }

    public static List<ModuleSlot> asSortedList(Map<String, ModuleSlot> map) {
        return map.entrySet().stream().sorted(Comparator.comparingDouble(a -> a.getValue().priority)).map(Map.Entry::getValue).toList();
    }

    @Override
    public Map<String, ModuleSlot> merge(Map<String, ModuleSlot> left, Map<String, ModuleSlot> right, MergeType mergeType) {
        return ModuleProperty.mergeMap(left, right, mergeType);
    }

    public Map<String, ModuleSlot> initialize(Map<String, ModuleSlot> property, ModuleInstance context) {
        property.forEach((id, slot) -> slot.initialize(context));
        List<Map.Entry<String, ModuleSlot>> slots = property.entrySet().stream().sorted(Comparator.comparingDouble(a -> a.getValue().priority)).toList();
        Map<String, ModuleSlot> initializedMap = new LinkedHashMap<>();
        for (var value : slots) {
            value.getValue().initialize(context);
            initializedMap.put(value.getKey(), value.getValue());
        }
        return initializedMap;
    }

    public static class ModuleSlot {
        @CodecBehavior.Optional
        public Transform transform = Transform.IDENTITY;
        @CodecBehavior.Optional
        public String translationKey = "miapi.module.empty.name";
        @CodecBehavior.Optional
        public String slotType = "default";
        @CodecBehavior.Optional
        public List<String> allowed = new ArrayList<>();
        @CodecBehavior.Optional
        public List<String> allowedMerge = new ArrayList<>();
        @CodecBehavior.Optional
        public double priority = 0.0;
        @AutoCodec.Ignored
        public String id;
        @Nullable
        @AutoCodec.Ignored
        public ModuleInstance inSlot;
        @Nullable
        @AutoCodec.Ignored
        public ModuleInstance parent;

        public ModuleSlot(List<String> allowedList) {
            this.allowed = allowedList;
            id = "primary";
        }


        public ModuleSlot() {
            this.allowed = new ArrayList<>();
            id = "primary";
        }

        public boolean allowedIn(ModuleInstance instance) {
            List<String> allowedSlots = AllowedSlots.getAllowedSlots(instance.module);
            for (String key : allowed) {
                if (allowedSlots.contains(key)) {
                    return true;
                }
            }
            return false;
        }

        public void initialize(ModuleInstance moduleInstance) {
            inSlot = moduleInstance.getSubModule(id);
            parent = moduleInstance;
        }

        @Environment(EnvType.CLIENT)
        public TransformMap getTransformStack() {
            TransformMap stack = new TransformMap();
            stack.add(transform);
            return stack;
        }

        public List<String> getAsLocation() {
            List<String> location = new ArrayList<>();
            location.add(id);
            ModuleInstance parsing = parent;
            if(parsing!=null){
                while (parsing.getParent() != null) {
                    String slotNumber = SlotProperty.getSlotID(parsing);
                    location.add(slotNumber);
                    parsing = parsing.getParent();
                }
            }
            return location;
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
                    return sortedAllowed.equals(sortedOtherAllowed);
                }
                return true;
            }
            return false;
        }
    }
}
