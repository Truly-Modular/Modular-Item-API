package smartin.miapi.registries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.util.InternalApi;
import org.jetbrains.annotations.Nullable;
import oshi.annotation.concurrent.NotThreadSafe;
import smartin.miapi.Miapi;

import java.util.*;
import java.util.function.*;

/**
 * A generic registry class that can be used to store and retrieve entries by name.
 * Entries can be of any type, and a registry can be accessed globally through its associated class.
 *
 * @param <T> the type of the entries to store in the registry
 */
public class MiapiRegistry<T> {
    /**
     * The map of entries stored in this registry, indexed by name.
     */
    protected final Map<ResourceLocation, T> entries = Collections.synchronizedMap(new LinkedHashMap<>());
    /**
     * The map of all MiapiRegistry instances, indexed by class type.
     */
    @InternalApi
    public static final Map<Class<?>, MiapiRegistry<?>> REGISTRY_MAP = Collections.synchronizedMap(new LinkedHashMap<>());
    /**
     * The list of callbacks to invoke when new entries are added to the registry.
     */
    protected final List<Consumer<T>> callbacks = new ArrayList<>();

    protected final List<BiConsumer<ResourceLocation, T>> idCallbacks = new ArrayList<>();

    @Nullable
    protected final Class<T> tClass;

    /**
     * Protected constructor to prevent direct instantiation of the registry.
     */
    protected MiapiRegistry(Class<T> tClass) {
        this.tClass = tClass;
    }

    protected MiapiRegistry() {
        this.tClass = null;
    }

    /**
     * Returns the instance of the MiapiRegistry associated with the specified class type.
     *
     * @param clazz the class type to retrieve the registry instance for
     * @param <T>   the type of the entries to store in the registry
     * @return the instance of the MiapiRegistry associated with the specified class type
     */
    public static <T> MiapiRegistry<T> getInstance(Class<T> clazz) {
        if (!REGISTRY_MAP.containsKey(clazz)) {
            MiapiRegistry<T> instance = new MiapiRegistry<>(clazz);
            REGISTRY_MAP.put(clazz, instance);
            return instance;
        }
        return (MiapiRegistry<T>) REGISTRY_MAP.get(clazz);
    }

    /**
     * Returns the key associated with the specified value, or null if no key was found.
     *
     * @param value the value to search for
     * @param <T>   the type of the entries to store in the registry
     * @return the key associated with the specified value, or null if no key was found
     */
    @Nullable
    public <T> ResourceLocation findKey(T value) {
        Optional<ResourceLocation> matchingId = entries.entrySet().stream()
                .filter(Objects::nonNull)
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> entry.getValue().equals(value))
                .map(Map.Entry::getKey)
                .findFirst();
        return matchingId.orElse(null);
    }

    /**
     * Returns the instance of the MiapiRegistry associated with the specified class type, and adds the specified
     * callbacks to be invoked when new entries are added to the registry.
     *
     * @param clazz     the class type to retrieve the registry instance for
     * @param callbacks the list of callbacks to invoke when new entries are added to the registry
     * @param <T>       the type of the entries to store in the registry
     * @return the instance of the MiapiRegistry associated with the specified class type
     */
    public static <T> MiapiRegistry<T> getInstance(Class<T> clazz, List<Consumer<T>> callbacks) {
        MiapiRegistry<T> instance;
        if (!REGISTRY_MAP.containsKey(clazz)) {
            instance = new MiapiRegistry<T>(clazz);
            REGISTRY_MAP.put(clazz, instance);
        }
        instance = (MiapiRegistry<T>) REGISTRY_MAP.get(clazz);
        MiapiRegistry<T> finalInstance = instance;
        callbacks.forEach(finalInstance::addCallback);
        return (MiapiRegistry<T>) REGISTRY_MAP.computeIfAbsent(clazz, (T) -> new MiapiRegistry<T>(clazz));
    }

    /**
     * Registers a new entry with the given name and value to this registry. If an entry with the same name already exists, an
     * IllegalArgumentException is thrown. Calls all the callbacks associated with the class type.
     *
     * @param name  the name of the entry to be registered
     * @param value the value of the entry to be registered
     * @throws IllegalArgumentException if an entry with the same name already exists
     */
    public T register(String name, T value) {
        return register(Miapi.id(name), value);
    }

    /**
     * Registers a new entry with the given name and value to this registry. If an entry with the same name already exists, an
     * IllegalArgumentException is thrown. Calls all the callbacks associated with the class type.
     *
     * @param name  the name of the entry to be registered
     * @param value the value of the entry to be registered
     * @throws IllegalArgumentException if an entry with the same name already exists
     */
    public T register(ResourceLocation name, T value) {
        if (entries.containsKey(name)) {
            throw new IllegalArgumentException("Entry with name '" + name + "' already exists.");
        }
        entries.put(name, value);

        // Call the callbacks for the class type
        callbacks.forEach(callback -> callback.accept(value));
        idCallbacks.forEach(callback -> callback.accept(name, value));
        return value;
    }

    /**
     * Threadsave replacing an existing entry
     *
     * @param id       the name of the entry to be registered
     * @param newEntry the new entry to be registered
     */
    public void replace(ResourceLocation id, T newEntry) {
        entries.compute(id, (i, old) -> newEntry);
        callbacks.forEach(callback -> callback.accept(newEntry));
        idCallbacks.forEach(callback -> callback.accept(id, newEntry));
    }

    /**
     * Threadsave replacing an existing entry
     *
     * @param id                the name of the entry to be registered
     * @param remappingFunction remapping function, old entry can be null!
     */
    public void replace(ResourceLocation id, BiFunction<ResourceLocation, ? super T, ? extends T> remappingFunction) {
        entries.compute(id, (i, entry) -> {
            T newEntry = remappingFunction.apply(i, entry);
            callbacks.forEach(callback -> callback.accept(newEntry));
            idCallbacks.forEach(callback -> callback.accept(id, newEntry));
            return newEntry;
        });
    }

    public boolean containsKey(ResourceLocation id) {
        return entries.containsKey(id);
    }

    /**
     * Removes all entries from this registry.
     */
    public void clear() {
        entries.clear();
    }

    /**
     * Retrieves the entry associated with the given name from this registry. If no such entry exists, returns null.
     *
     * @param name the name of the entry to be retrieved
     * @return the entry associated with the given name, or null if no such entry exists
     */
    @Nullable
    public T get(ResourceLocation name) {
        return entries.get(name);
    }

    /**
     * Retrieves the entry associated with the given name from this registry. If no such entry exists, returns null.
     *
     * @param name the name of the entry to be retrieved
     * @return the entry associated with the given name, or null if no such entry exists
     */
    @Nullable
    public T get(String name) {
        return get(Miapi.id(name));
    }

    public T remove(ResourceLocation id) {
        return entries.remove(id);
    }

    /**
     * Adds a new callback to this registry. The callback will be called for every entry in the registry, and for all
     * entries that are registered in the future. The callback will be called immediately for all existing entries in the
     * registry.
     *
     * @param callback the callback to be added
     */
    public void addCallback(Consumer<T> callback) {
        callbacks.add(callback);
        entries.values().forEach(callback);
    }

    public void addCallback(BiConsumer<ResourceLocation, T> callback) {
        idCallbacks.add(callback);
        entries.forEach(callback::accept);
    }

    public String getName() {
        for (Class key : REGISTRY_MAP.keySet()) {
            if (REGISTRY_MAP.get(key) == this) {
                return key.getName();
            }
        }
        return "Unknown Registry";
    }

    /**
     * Returns a flat map of all entries in this registry, with the entry names as keys and the entry values as values.
     * this is the registries internal map, so this can be used to edit the entries.
     * WARNING! calling entry() or similar iterators is not threadsve
     *
     * @return a map of all entries in this registry
     */
    @NotThreadSafe
    public Map<ResourceLocation, T> getFlatMap() {
        return entries;
    }

    public Codec<T> codec() {
        return ResourceLocation.CODEC.xmap(
                this::get,
                this::findKey
        );
    }

    public Codec<T> dispatchCodec(Function<T, MapCodec<? extends T>> codecFunction) {
        return ResourceLocation.CODEC.dispatch((a -> {
            ResourceLocation id = findKey(a);
            if (id == null) {
                throw new IllegalArgumentException("Unknown type: " + a.toString() + " for Type " + this.getName());
            }
            return findKey(a);
        }), (id -> {
            T data = get(id);
            if (data == null) {
                throw new IllegalArgumentException("Unknown type: " + id + " for Type " + this.getName());
            }
            MapCodec<? extends T> codec = codecFunction.apply(data);
            if (codec == null) {
                throw new IllegalArgumentException("Could not encode: " + id + " for Type " + this.getName());
            }
            return codec;
        }));
    }
}