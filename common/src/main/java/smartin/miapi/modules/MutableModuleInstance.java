package smartin.miapi.modules;

import com.google.gson.JsonElement;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Mutable version of {@link ModuleInstance}
 * This has no cache or ability to read modules.
 * if that is desired, quickly create an Immutable and read from there.
 * this ensures cache persistence
 */
public class MutableModuleInstance {

    private volatile ResourceLocation moduleId;
    private final Map<ResourceLocation, JsonElement> data = new ConcurrentHashMap<>();
    private final Map<String, MutableModuleInstance> children = new ConcurrentHashMap<>();
    private final RegistryOps.RegistryInfoLookup getter;
    private volatile MutableModuleInstance parent;

    public MutableModuleInstance(ResourceLocation moduleId, RegistryAccess access) {
        this(moduleId, new MiapiHolderLookupAdapter(access));
    }

    public MutableModuleInstance(ResourceLocation moduleId, RegistryOps.RegistryInfoLookup getter) {
        this.moduleId = moduleId;
        this.getter = getter;
    }

    /**
     * creates a Mutable copy.
     * this copies the entire module tree and returns the mutable at the same position.
     */
    public static MutableModuleInstance fromRecord(ModuleInstance record) {
        List<String> pos = new ArrayList<>();
        record.calculatePosition(pos);
        return fromRecordInternal(record.getRoot()).getPosition(pos);
    }


    private static MutableModuleInstance fromRecordInternal(ModuleInstance record) {
        MutableModuleInstance root = new MutableModuleInstance(record.moduleId(), record.getter());

        // copy data
        root.data.putAll(record.data());

        // recursively copy children
        for (var entry : record.children().entrySet()) {
            MutableModuleInstance childMutable = fromRecordInternal(entry.getValue());
            childMutable.parent = root;
            root.children.put(entry.getKey(), childMutable);
        }

        return root;
    }

    public void setChild(String slot, MutableModuleInstance child) {
        child.parent = this;
        children.put(slot, child);
    }

    public void removeChild(String slot) {
        MutableModuleInstance child = children.remove(slot);
        if (child != null) {
            child.parent = null;
        }
    }

    public MutableModuleInstance getChild(String slot) {
        return children.get(slot);
    }

    /**
     * @return internal mutable map (modifications must maintain parent consistency)
     */
    public Map<String, MutableModuleInstance> getChildren() {
        return children;
    }

    public MutableModuleInstance getParent() {
        return parent;
    }

    public void setData(ResourceLocation key, JsonElement value) {
        data.put(key, value);
    }

    public JsonElement getData(ResourceLocation key) {
        return data.get(key);
    }

    /**
     * Replaces all data entries.
     * The provided map is copied; subsequent modifications to it will not affect this instance.
     */
    public void setDataMap(Map<ResourceLocation, JsonElement> newData) {
        data.clear();
        data.putAll(newData);
    }

    public void removeData(ResourceLocation key) {
        data.remove(key);
    }

    /**
     * Converts this mutable tree to an immutable ModuleInstance.
     *
     * Returns the node at the same logical position in the resulting tree
     * Nodes with moduleId namespace "empty" and no children are pruned
     */
    public ModuleInstance toRecord() {
        List<String> pos = calculatePosition();
        MutableModuleInstance root = this;
        while (root.parent != null) {
            root = root.parent;
        }
        return root.toRecordInternal().getPosition(pos);
    }

    private ModuleInstance toRecordInternal() {
        Map<String, ModuleInstance> childRecords = new LinkedHashMap<>();

        for (var entry : children.entrySet()) {
            if ("empty".equals(entry.getValue().moduleId.getNamespace())) {
                if (entry.getValue().getChildren().isEmpty()) {
                    continue;
                }
            }
            ModuleInstance childInstance = entry.getValue().toRecordInternal();
            childRecords.put(entry.getKey(), childInstance);
        }

        return new ModuleInstance(
                moduleId,
                Map.copyOf(data),
                childRecords,
                getter
        );
    }


    /**
     * Retrieves the mutable module instance at the specified position in the hierarchy.
     *
     * @param position The position of the module instance.
     * @return The module instance at the specified position.
     */
    @Nullable
    public MutableModuleInstance getPosition(List<String> position) {
        if (position == null || position.isEmpty()) {
            return this;
        }
        MutableModuleInstance current = this;
        for (String key : position) {
            current = current.children.get(key);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    /**
     * full list of all submodules and their submodules.
     * does not parse to parents.
     * this is not sorted.
     * for sorted iteration convert to a Immutable first.
     * Mutables cant be sorted as they arent allowed to have caches.
     * @return new list (modifications do not affect internal state)
     */
    public List<MutableModuleInstance> getUnsortedList() {
        List<MutableModuleInstance> nextFlatList = new ArrayList<>();
        Deque<MutableModuleInstance> queue = new ArrayDeque<>();
        queue.add(this);

        while (!queue.isEmpty()) {
            MutableModuleInstance module = queue.removeFirst();
            if (module != null) {
                nextFlatList.add(module);
                for(MutableModuleInstance child :module.getChildren().values()){
                    queue.addFirst(child);
                }
            }
        }
        return nextFlatList;
    }

    /**
     * Recursively calculates the position of this module instance in its hierarchy.
     *
     * @param position The list to store the position.
     */
    public List<String> calculatePosition(List<String> position) {
        MutableModuleInstance parsingInstance = getParent();
        if (parsingInstance != null) {
            for (Map.Entry<String, MutableModuleInstance> entry : parsingInstance.children.entrySet()) {
                if (entry.getValue() == this) {
                    parsingInstance.calculatePosition(position);
                    position.add(entry.getKey());
                    return position;
                }
            }
        }
        return position;
    }

    /**
     * @return a Mutable List of strings used to parse its position
     * Each call to this method creates a new List.
     */
    public List<String> calculatePosition() {
        return calculatePosition(new ArrayList<>());
    }

    /**
     * Sets the module identifier.
     * Does not affect structure or children.
     */
    public void setModule(ResourceLocation id) {
        this.moduleId = id;
    }
}