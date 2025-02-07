package smartin.miapi.client.model.item;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class DualKeyCache<K1, K2, V> {
    private final Map<K1, Map<K2, V>> cache = new HashMap<>();
    private final Map<K1, V> singleKeyCache = new HashMap<>();

    public V getNullSave(K1 key1, K2 key2, BiFunction<K1, K2, V> compute) {
        if (key2 == null) {
            return singleKeyCache.computeIfAbsent(key1, (k) -> compute.apply(k, key2));
        }
        Map<K2, V> innerMap = cache.computeIfAbsent(key1, k1 -> new HashMap<>());
        return innerMap.computeIfAbsent(key2, k2 -> compute.apply(key1, key2));
    }

    public void put(K1 key1, K2 key2, V value) {
        cache.computeIfAbsent(key1, k -> new HashMap<>()).put(key2, value);
    }

    public boolean contains(K1 key1, K2 key2) {
        Map<K2, V> innerMap = cache.get(key1);
        return innerMap != null && innerMap.containsKey(key2);
    }
}
