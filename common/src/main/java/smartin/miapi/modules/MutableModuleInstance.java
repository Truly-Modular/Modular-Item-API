package smartin.miapi.modules;

import com.google.gson.JsonElement;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MutableModuleInstance {

    private ResourceLocation moduleId;
    private final Map<ResourceLocation, JsonElement> data = new ConcurrentHashMap<>();
    private final Map<String, MutableModuleInstance> children = new ConcurrentHashMap<>();
    private final RegistryOps.RegistryInfoLookup getter;

    private MutableModuleInstance parent;

    public MutableModuleInstance(ResourceLocation moduleId, RegistryAccess access) {
        this(moduleId,new MiapiHolderLookupAdapter(access));
    }

    public MutableModuleInstance(ResourceLocation moduleId, RegistryOps.RegistryInfoLookup getter) {
        this.moduleId = moduleId;
        this.getter = getter;
    }

    public static MutableModuleInstance fromRecord(ModuleInstance record) {
        List<String> pos = new ArrayList<>();
        record.calculatePosition(pos);
        MutableModuleInstance mutableModuleInstance =fromRecordInternal(record.getRoot()).getPosition(pos);
        if(!record.moduleId().equals(mutableModuleInstance.moduleId)){
            Miapi.LOGGER.error("error");
        }
        return mutableModuleInstance;
    }


    private static MutableModuleInstance fromRecordInternal(ModuleInstance record) {
        List<String> pos = new ArrayList<>();
        record.calculatePosition(new ArrayList<>());
        record.getRoot();

        MutableModuleInstance root = new MutableModuleInstance(record.moduleId(), record.getter());

        // copy data
        record.data().forEach(root.data::put);

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
        children.remove(slot);
    }

    public MutableModuleInstance getChild(String slot) {
        return children.get(slot);
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

    public void setDataMap(Map<ResourceLocation, JsonElement> newData) {
        data.clear();
        data.putAll(newData);
    }

    public void removeData(ResourceLocation key) {
        data.remove(key);
    }

    public Map<String, MutableModuleInstance> getChildren() {
        return children;
    }

    public ModuleInstance toRecord() {
        List<String> pos = calculatePosition();
        MutableModuleInstance root = this;
        while(root.parent!=null){
            root = root.parent;
        }
        ModuleInstance moduleInstance =root.toRecordInternal().getPosition(pos);
        if(!moduleInstance.moduleId().equals(this.moduleId)){
            Miapi.LOGGER.error("error");
        }
        return moduleInstance;
    }

    private ModuleInstance toRecordInternal() {
        Map<String, ModuleInstance> childRecords = new LinkedHashMap<>();

        for (var entry : children.entrySet()) {
            ModuleInstance childInstance = entry.getValue().toRecordInternal();
            childRecords.put(entry.getKey(), childInstance);
            childInstance.getParent();
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
     * this is not sorted.
     * mutables dont know thrir sorting yet.
     * for sorted iteration convert to a Immutable first.
     * @return
     */
    public List<MutableModuleInstance> getUnsortedList(){
        List<MutableModuleInstance> nextFlatList = new ArrayList<>();
        List<MutableModuleInstance> queue = new ArrayList<>();
        queue.add(this);

        while (!queue.isEmpty()) {
            MutableModuleInstance module = queue.removeFirst();
            if (module != null) {
                nextFlatList.add(module);
                queue.addAll(0, module.getChildren().values());
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
        MutableModuleInstance parssingParent = getParent();
        if (parssingParent != null) {
            for (Map.Entry<String, MutableModuleInstance> entry : parssingParent.children.entrySet()) {
                if (entry.getValue() == this) {
                    parssingParent.calculatePosition(position);
                    position.add(entry.getKey());
                    return position;
                }
            }
        }
        return position;
    }

    public List<String> calculatePosition() {
        return calculatePosition(new ArrayList<>());
    }

    public void setModule(ResourceLocation id) {
        this.moduleId = id;
    }
}