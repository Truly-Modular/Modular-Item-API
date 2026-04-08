package smartin.miapi.modules.properties.util;

import org.apache.commons.lang3.function.TriFunction;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Merge able is a common interface to merge incoming data from different sources.
 * In many cases there are multiple sources that give context data for Properties or similar things.
 * f.e. multiple modules setting the same thing.
 * This is the Structure to allow for intelligent data-merging behaviour
 *
 * @param <T> The Type to be Merged
 */
public interface MergeAble<T> {

    /**
     * RETURN A COPY OF EITHER OBJECT.
     * DO NOT CHANGE left or right
     *
     * @param left      immutable left side
     * @param right     immutable right side
     * @param mergeType the Type of Merging behaviour
     * @return a merged copy of {@link T}
     */
    T merge(T left, T right, MergeType mergeType);

    /**
     * Simplified Merging of Lists, adds both entries if merging type requires it
     *
     * @return the Merged List
     */
    static <K> List<K> mergeList(List<K> left, List<K> right, MergeType mergeType) {
        if (MergeType.OVERWRITE.equals(mergeType)) {
            return new ArrayList<>(right);
        }
        List<K> merged = new ArrayList<>(left);
        merged.addAll(right);
        return merged;
    }

    /**
     * Simplified Merging of Maps, on collision uses left right decide for entries
     *
     * @return the Merged Linked Map as to not disturb ordering
     */
    static <K, L> Map<L, K> mergeMap(Map<L, K> left, Map<L, K> right, MergeType mergeType) {
        return mergeMap(left, right, mergeType, (key, l, r) -> decideLeftRight(l, r, mergeType));
    }

    /**
     * Raw Merging of Maps, custom behaviour on collisions is possible
     *
     * @param onCollision first is the key of the collision, then left, right and returned the resolved collision.
     *                    if null is returned nothing is added to the Map
     * @return the Merged Linked Map as to not disturb ordering
     */
    static <K, L> Map<L, K> mergeMap(Map<L, K> left, Map<L, K> right, MergeType mergeType, TriFunction<L, K, K, K> onCollision) {
        if (MergeType.OVERWRITE.equals(mergeType)) {
            return new LinkedHashMap<>(right);
        }
        Map<L, K> merged = new LinkedHashMap<>(left);
        right.forEach((key, entry) -> {
            if (!merged.containsKey(key)) {
                merged.put(key, entry);
            } else {
                K mergedKey = onCollision.apply(key, merged.get(key), entry);
                if (mergedKey != null) {
                    merged.put(key, mergedKey);
                } else {
                    merged.remove(key);
                }
            }
        });
        return merged;
    }

    /**
     * Simply decides between to optiions
     * !!!WARNING!!! make sure to only use this for Immutable Objects!!!
     */
    @SuppressWarnings("unchecked")
    static <K> K decideLeftRight(K right, K left, MergeType mergeType) {
        if (right instanceof Optional<?> r && left instanceof Optional<?> l) {
            if (r.isEmpty() && l.isPresent()) {
                return (K) l;
            }
            if (r.isPresent() && l.isEmpty()) {
                return (K) r;
            }
        }
        if (MergeType.EXTEND.equals(mergeType)) {
            return right;
        } else {
            return left;
        }
    }

    /**
     * Reflectively merges two instances by delegating field handling to the appropriate merge strategy.
     *
     * <p>Resolution is automatic per field:</p>
     * {@link MergeAble} → delegated to {@link MergeAble#merge(Object, Object, MergeType)}
     * WARNING -> assumes Foo Mergeable<Foo> implementation, nothing else
     * {@link Map} → delegated to {@link #mergeMap(Map, Map, MergeType)}
     * {@link List} → delegated to {@link #mergeList(List, List, MergeType)}
     * otherwise → {@link MergeAble#decideLeftRight(Object, Object, MergeType)}
     *
     * <p><b>Warnings / limitations:</b></p>
     * <ul>
     *     <li>Requires a no-arg constructor and non-final fields</li>
     *     <li>Uses reflection (performance overhead, no compile-time safety)</li>
     *     <li>Shallow field scan only (no deep object graph guarantees beyond delegation)</li>
     *     <li>Type erasure limits precise generic handling (especially for collections)</li>
     *     <li>Undefined behavior if field values are incompatible between left/right</li>
     * </ul>
     */
    static <T> T autoMerge(T left, T right, MergeType mergeType, Class<T> clazz) {
        try {
            T result = clazz.getDeclaredConstructor().newInstance();

            for (Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);

                Object l = f.get(left);
                Object r = f.get(right);
                Object value;

                if (l == null || r == null) {
                    value = MergeAble.decideLeftRight(l, r, mergeType);
                }
                else if (l instanceof MergeAble<?> ml && r instanceof MergeAble<?>) {
                    value = ((MergeAble<Object>) ml).merge(l, r, mergeType);
                }
                else if (l instanceof Map<?, ?> && r instanceof Map<?, ?>) {
                    value = unsafeMergeMap(l, r, mergeType);
                }
                else if (l instanceof List<?> && r instanceof List<?>) {
                    value = unsafeMergeList(l, r, mergeType);
                }
                else {
                    value = MergeAble.decideLeftRight(l, r, mergeType);
                }

                f.set(result, value);
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("autoMerge failed for " + clazz.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Object unsafeMergeMap(Object l, Object r, MergeType mergeType) {
        return mergeMap((Map<Object, Object>) l, (Map<Object, Object>) r, mergeType);
    }

    @SuppressWarnings("unchecked")
    private static Object unsafeMergeList(Object l, Object r, MergeType mergeType) {
        return mergeList((List<Object>) l, (List<Object>) r, mergeType);
    }
}