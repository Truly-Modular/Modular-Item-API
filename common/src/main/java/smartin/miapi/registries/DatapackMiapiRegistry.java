package smartin.miapi.registries;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import oshi.annotation.concurrent.ThreadSafe;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class DatapackMiapiRegistry<T> extends MiapiRegistry<T> {

    protected final Map<ResourceLocation, T> tempEntries =
            Collections.synchronizedMap(new LinkedHashMap<>());
    protected Map<ResourceLocation, T> merged;

    public DatapackMiapiRegistry(Class<T> tClass) {
        super(tClass);
    }

    public DatapackMiapiRegistry() {
        super();
    }

    /**
     * Returns the instance of the DatapackMiapiRegistry associated with the specified class type.
     * of a MiapiRegistry already exists for this, it is replaced and its entries and callbacks are copied into this.
     *
     * @param clazz the class type to retrieve the registry instance for
     * @param <T>   the type of the entries to store in the registry
     * @return the instance of the DatapackMiapiRegistry associated with the specified class type
     */
    public static <T> DatapackMiapiRegistry<T> getInstance(Class<T> clazz) {
        if (REGISTRY_MAP.get(clazz) instanceof DatapackMiapiRegistry datapackMiapiRegistry) {
            return (DatapackMiapiRegistry<T>) datapackMiapiRegistry;
        }
        DatapackMiapiRegistry<T> instance = new DatapackMiapiRegistry<>(clazz);
        if (REGISTRY_MAP.get(clazz) != null) {
            REGISTRY_MAP.get(clazz).entries.forEach((id, entry) ->
                    instance.register(id, (T) entry));
            REGISTRY_MAP.get(clazz).callbacks.forEach(callback ->
                    instance.addCallback((Consumer<T>) callback));
        }
        REGISTRY_MAP.put(clazz, instance);
        return instance;
    }


    public T registerTemporary(ResourceLocation id, T value) {
        if (tempEntries.containsKey(id) || entries.containsKey(id)) {
            throw new IllegalArgumentException("Entry already exists: " + id);
        }

        tempEntries.put(id, value);

        callbacks.forEach(cb -> cb.accept(value));
        idCallbacks.forEach(cb -> cb.accept(id, value));
        merged = null;

        return value;
    }

    @Override
    @Nullable
    public T get(ResourceLocation id) {
        // TEMP entries
        if (tempEntries.containsKey(id)) {
            return tempEntries.get(id);
        }

        // FALLBACK to permanent
        return super.get(id);
    }

    /**
     * Threadsave replacing an existing entry
     *
     * @param id                the name of the entry to be registered
     * @param remappingFunction remapping function, old entry can be null!
     */
    public void replaceTemporary(ResourceLocation id, BiFunction<ResourceLocation, ? super T, ? extends T> remappingFunction) {
        tempEntries.compute(id, (i, entry) -> {
            T newEntry = remappingFunction.apply(i, entry);
            callbacks.forEach(callback -> callback.accept(newEntry));
            idCallbacks.forEach(callback -> callback.accept(id, newEntry));
            return newEntry;
        });
        merged = null;
    }

    @Override
    public boolean containsKey(ResourceLocation id) {
        return tempEntries.containsKey(id) || super.containsKey(id);
    }

    /**
     * clears temporary data
     */
    public void clearTemporary() {
        tempEntries.clear();
        merged = null;
    }

    /**
     * clears temporary and permanent Data
     */
    @Override
    public void clear() {
        super.clear();
        clearTemporary();
    }

    /**
     * unlike parent method, this returns a copy of the map instead of the actual map.
     * the copy is mutable, but should NOT be treated as such, as it modifies the cached value.
     */
    @ThreadSafe
    @Override
    public Map<ResourceLocation, T> getFlatMap() {
        synchronized (tempEntries){
            if (this.merged == null) {
                Map<ResourceLocation, T> nextMerged = new LinkedHashMap<>(super.getFlatMap());
                nextMerged.putAll(tempEntries);
                this.merged = nextMerged;
            }
        }
        return merged;
    }
}