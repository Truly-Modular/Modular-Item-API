package smartin.miapi.modules.properties.util;

import org.apache.commons.lang3.function.TriFunction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    static <K> K decideLeftRight(K right, K left, MergeType mergeType) {
        if (MergeType.EXTEND.equals(mergeType)) {
            return right;
        } else {
            return left;
        }
    }
}