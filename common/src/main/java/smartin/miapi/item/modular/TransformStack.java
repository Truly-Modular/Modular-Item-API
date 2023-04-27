package smartin.miapi.item.modular;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * A stack of transformations, represented as a mapping of strings to {@link Transform}.
 * The `primary` field specifies the ID of the primary transformation in the stack.
 */
public class TransformStack {
    /**
     * The stack of {@link Transform}, represented as a mapping of strings to {@link Transform}s.
     */
    protected Map<String,Transform> stack = new HashMap<>();
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
    @Nonnull
    public Transform get(String id){
        return stack.computeIfAbsent(id,(key)->Transform.IDENTITY);
    }

    @Nonnull
    public Transform get(){
        return stack.computeIfAbsent(primary,(key)->Transform.IDENTITY);
    }

    public static TransformStack merge(TransformStack parent, TransformStack toMerge){
        TransformStack merged = parent.copy();
        toMerge.stack.forEach((id,transform)->{
            merged.add(id,transform);
        });
        return merged;
    }

    /**
     * Returns `true` if the specified ID is present in the stack, `false` otherwise.
     *
     * @param id the ID to check for
     * @return `true` if the specified ID is present in the stack, `false` otherwise
     */
    public boolean isPresent(String id){
        return stack.containsKey(id);
    }

    /**
     * Sets the {@link Transform} associated with the specified ID to the specified {@link Transform}.
     *
     * @param id the ID of the {@link Transform} to set
     * @param transform the {@link Transform} to set
     */
    public void set(String id,Transform transform){
        stack.put(id,transform);
    }

    /**
     * Adds the specified {@link Transform} to the {@link Transform} associated with the specified ID.
     *
     * @param id the ID of the {@link Transform} to add to
     * @param transform the `{@link Transform} to add
     */
    public void add(String id,Transform transform){
        Transform old = get(id);
        set(id,Transform.merge(old,transform));
    }

    public void add(Transform transform){
        add(transform.origin,transform);
        if(transform.origin!=null){
            primary = transform.origin;
        }
    }


    public TransformStack copy(){
        TransformStack newStack = new TransformStack();
        stack.forEach((id,transform)->{
            newStack.add(id,transform.copy());
        });
        return newStack;
    }
}
