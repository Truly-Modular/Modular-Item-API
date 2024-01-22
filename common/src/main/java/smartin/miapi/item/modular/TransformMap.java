package smartin.miapi.item.modular;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A map of Transforms, represented as a mapping of strings to {@link Transform}.
 * The `primary` field specifies the ID of the primary transformation in the stack.
 */
public class TransformMap {
    /**
     * The stack of {@link Transform}, represented as a mapping of strings to {@link Transform}s.
     */
    protected Map<String, Transform> stack = new HashMap<>();
    /**
     * The ID of the primary {@link Transform} in the stack.
     */
    public String primary = null;

    /**
     * Returns the {@link Transform} associated with the specified ID.
     * If the ID is not present in the stack, a new {@link Transform#IDENTITY} is added to the stack with that ID.
     *
     * @param id the ID of the {@link Transform} to retrieve
     * @return the {@link Transform} associated with the specified ID
     */
    public Transform get(String id) {
        return stack.computeIfAbsent(id, (key) -> Transform.IDENTITY);
    }

    /**
     * Returns the primary {@link Transform} .
     * If the ID is not present in the stack, a new {@link Transform#IDENTITY} is added to the stack with that ID.
     *
     * @return the primary {@link Transform}
     */
    public Transform get() {
        return stack.computeIfAbsent(primary, (key) -> Transform.IDENTITY);
    }

    public static TransformMap merge(TransformMap parent, TransformMap toMerge) {
        TransformMap merged = parent.copy();
        toMerge.stack.forEach(merged::add);
        return merged;
    }

    /**
     * Returns `true` if the specified ID is present in the stack, `false` otherwise.
     *
     * @param id the ID to check for
     * @return `true` if the specified ID is present in the stack, `false` otherwise
     */
    public boolean isPresent(String id) {
        return stack.containsKey(id);
    }

    /**
     * Sets the {@link Transform} associated with the specified ID to the specified {@link Transform}.
     *
     * @param id        the ID of the {@link Transform} to set
     * @param transform the {@link Transform} to set
     */
    public void set(String id, Transform transform) {
        stack.put(id, transform);
    }

    /**
     * Adds the specified {@link Transform} to the {@link Transform} associated with the specified ID.
     *
     * @param id        the ID of the {@link Transform} to add to
     * @param transform the `{@link Transform} to add
     */
    public void add(String id, Transform transform) {
        Transform old = get(id);
        set(id, Transform.merge(old, transform));
    }

    /**
     * Adds the specified {@link Transform} to the {@link Transform} associated with the Transforms origin.
     *
     * @param transform the `{@link Transform} to add
     */
    public void add(Transform transform) {
        add(transform.origin, transform);
        if (transform.origin != null) {
            primary = transform.origin;
        }
    }

    /**
     * @return a deep copy of the TransformMap
     */
    public TransformMap copy() {
        TransformMap newStack = new TransformMap();
        stack.forEach((id, transform) -> {
            newStack.add(id, transform.copy());
        });
        return newStack;
    }
}
