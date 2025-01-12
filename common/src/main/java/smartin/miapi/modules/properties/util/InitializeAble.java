package smartin.miapi.modules.properties.util;

import smartin.miapi.modules.ModuleInstance;

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
     * @param data    the un-initialized data
     * @param context the ModuleInstance context for this InitializeAble
     * @return
     */
    T initialize(ModuleInstance context, T data);
}
