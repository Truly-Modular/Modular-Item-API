package smartin.miapi.registries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MiapiRegistry<T> {
    protected final Map<String, T> entries = new HashMap<>();
    protected static final Map<Class<?>, MiapiRegistry<?>> REGISTRY_MAP = new HashMap<>();
    protected final List<Consumer<T>> callbacks = new ArrayList<>();

    protected MiapiRegistry() {
        // Private constructor to prevent direct instantiation
    }

    public static <T> MiapiRegistry<T> getInstance(Class<T> clazz) {
        if (!REGISTRY_MAP.containsKey(clazz)) {
            MiapiRegistry<T> instance = new MiapiRegistry<T>();
            REGISTRY_MAP.put(clazz, instance);
            return instance;
        }
        return (MiapiRegistry<T>) REGISTRY_MAP.get(clazz);
    }

    public static <T> MiapiRegistry<T> getInstance(Class<T> clazz, List<Consumer<T>> callbacks) {
        MiapiRegistry<T> instance;
        if (!REGISTRY_MAP.containsKey(clazz)) {
            instance = new MiapiRegistry<T>();
            REGISTRY_MAP.put(clazz, instance);
        }
        instance = (MiapiRegistry<T>) REGISTRY_MAP.get(clazz);
        MiapiRegistry<T> finalInstance = instance;
        callbacks.forEach(callback -> finalInstance.addCallback(callback));
        return instance;
    }

    public void register(String name, T value) {
        if (entries.containsKey(name)) {
            throw new IllegalArgumentException("Entry with name '" + name + "' already exists.");
        }
        entries.put(name, value);

        // Call the callbacks for the class type
        callbacks.forEach(callback -> callback.accept(value));
    }

    public void clear(){
        entries.clear();
    }

    public T get(String name) {
        if (!entries.containsKey(name)) {
            return null;
            //throw new IllegalArgumentException("No entry found with name '" + name + "'.");
        }
        return entries.get(name);
    }

    public void addCallback(Consumer<T> callback) {
        callbacks.add(callback);
        entries.values().forEach(callback::accept);
    }

    public Map<String, T> getFlatMap(){
        return entries;
    }
}