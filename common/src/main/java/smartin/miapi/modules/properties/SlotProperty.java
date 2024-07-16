package smartin.miapi.modules.properties;

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
            current = current.parent;
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
            current = current.parent;
        }
        mergedTransform = TransformMap.merge(moduleSlot.getTransformStack(), mergedTransform);
        return mergedTransform;
    }

    public static Map<String, ModuleSlot> getSlots(ModuleInstance instance) {
        return getInstance().getData(instance).orElse(new HashMap<>());
    }

    public static String getSlotID(ModuleInstance instance) {
        if (instance.parent != null) {
            Map<String, ModuleSlot> slots = getSlots(instance.parent);
            AtomicReference<String> id = new AtomicReference<>("primary");
            slots.forEach((number, moduleSlot) -> {
                if (moduleSlot.inSlot == instance) {
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
        if (instance != null && instance.parent != null) {
            Map<String, ModuleSlot> slots = getSlots(instance.parent);
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

    @Override
    public Map<String, ModuleSlot> merge(Map<String, ModuleSlot> left, Map<String, ModuleSlot> right, MergeType mergeType) {
        return ModuleProperty.mergeMap(left, right, mergeType);
    }

    public Map<String, ModuleSlot> initialize(Map<String, ModuleSlot> property, ModuleInstance context) {
        property.forEach((id, slot) -> slot.initialize(context));
        return property;
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
            inSlot = moduleInstance;
            parent = moduleInstance.parent;
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
                    return sortedAllowed.equals(sortedOtherAllowed);
                }
                return true;
            }
            return false;
        }
    }
}
