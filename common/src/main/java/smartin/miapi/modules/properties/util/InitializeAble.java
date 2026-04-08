package smartin.miapi.modules.properties.util;

import smartin.miapi.modules.ModuleInstance;

import java.lang.reflect.Field;

/**
 * A small interface to implement the Initialize Function.
 * This is a new Behaviour in 1.21, allowing almost any data related thing to receive {@link ModuleInstance} context
 * This Context is required to resolve stats, other modules and many other things
 *
 * @param <T>
 */
public interface InitializeAble<T> {
    /**
     * this should return a copy of {@link T}!
     * DO NOT CHANGE THE GIVEN {@link T}!
     *
     * @param property   the un-initialized data
     * @param context the ModuleInstance context for this InitializeAble
     * @return
     */
    T initialize(T property, ModuleInstance context);


    /**
     * Reflectively initializes an instance by delegating field handling where possible.
     *
     * <p>Resolution is automatic per field:</p>
     * <ul>
     *     <li>{@link InitializeAble} → delegated to {@link InitializeAble#initialize(Object, ModuleInstance)}</li>
     *     <li>otherwise → value is copied as-is</li>
     * </ul>
     *
     * <p><b>Warnings / limitations:</b></p>
     * <ul>
     *     <li>Requires a no-arg constructor and non-final fields</li>
     *     <li>Uses reflection (performance overhead, no compile-time safety)</li>
     *     <li>Shallow field scan only (no deep object graph guarantees beyond delegation)</li>
     *     <li>Unsafe casts are used due to type erasure</li>
     *     <li>Undefined behavior if field values are incompatible with their declared types</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    static <T> T autoInitialize(T source, ModuleInstance ctx, Class<T> clazz) {
        try {
            T result = clazz.getDeclaredConstructor().newInstance();

            for (Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);
                Object val = f.get(source);

                if (val instanceof InitializeAble<?> init) {
                    val = unsafeInitialize(init, ctx);
                }
                if(val instanceof DoubleOperationResolvable doubleOperationResolvable){
                    val = doubleOperationResolvable.initialize(ctx);
                }

                f.set(result, val);
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("autoInitialize failed for " + clazz.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Object unsafeInitialize(Object init, ModuleInstance ctx) {
        return ((InitializeAble<Object>) init).initialize(init, ctx);
    }
}
