package smartin.miapi.events.property;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.reflect.AbstractInvocationHandler;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.datapack.codec.AutoCodec;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link ApplicationEvent}s are preset events used to simplify the process of creating {@link ModuleProperty}s that
 * trigger based on certain events. They have 3 type parameters, each equally important as the others.
 * @see ApplicationEvents
 *
 * @param <I> the invoker interface. Create an interface with a single void method with the parameters you want to input when calling this event.
 *            Make sure this interface only has this method, and no others.
 * @param <L> the listener object, usually also an interface. This is the object that will have to be passed in to actually add a listener.
 *            Again, you will usually want this to be an interface so that listeners can be added as lambdas.
 * @param <A> the additional data required to add a listener. For examples, see {@link ApplicationEvents}.
 */
@AutoCodec.Override("codec")
public abstract class ApplicationEvent<I, L, A> {
    public static final BiMap<String, ApplicationEvent<?, ?, ?>> allEvents = HashBiMap.create();
    public static final Codec<ApplicationEvent<?, ?, ?>> codec = Codec.STRING.xmap(
            s -> allEvents.get(s.toLowerCase().replaceAll("[.-]", "_")),
            e -> allEvents.inverse().get(e)
    );

    private final Class<I> invokerClass;
    public final String name;
    protected final I invoker;
    protected final List<Pair<L, A>> listeners = new ArrayList<>();

    /**
     * @param name the name of this ApplicationEvent
     * @param invokerGetter always leave this empty. It is used to help java detect the class of the invoker.
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public ApplicationEvent(String name, I... invokerGetter) {
        invokerClass = (Class<I>) invokerGetter.getClass().getComponentType();
        allEvents.put(name.toLowerCase().replaceAll("[.-]", "_"), this);
        this.name = name;
        invoker = (I) Proxy.newProxyInstance(invokerClass.getClassLoader(), new Class[]{invokerClass}, new AbstractInvocationHandler() {
            @Override
            protected Object handleInvocation(Object proxy, Method method, @Nullable Object[] args) throws Throwable {
                callEachListener(args);
                return null;
            }
        });
    }

    /**
     * @return the invoker for this event
     */
    public I invoker() {
        return invoker;
    }

    /**
     * Loops each listener and calls it, using the {@link #callWithInvokerParams(Object, Object[], Object)} method.
     * @param invokerArgs the args of the original invoker.
     */
    protected void callEachListener(Object[] invokerArgs) {
        listeners.forEach(pair -> {
            callWithInvokerParams(pair.getFirst(), invokerArgs, pair.getSecond());
        });
    }

    /**
     * A method used to call a listener, when provided with the parameters of the invoker as well as the additional data.
     *
     * @param toCall the listener to call
     * @param params the parameters of the original invoker
     * @param additionalData the additional data passed in with the listener. Set to null if the event doesn't use additional data.
     */
    protected abstract void callWithInvokerParams(L toCall, Object[] params, A additionalData);

    /**
     * A method to start listening to this event.
     *
     * @param listener the handler for when the event is called
     * @param additionalData additional data required on a per event basis.
     */
    public void startListening(L listener, A additionalData) {
        listeners.add(Pair.of(listener, additionalData));
    }

    /**
     * A template for ApplicationEvents that use the same listener as invoker, and have no additional data required.
     * @param <T> the invoker and listener interface
     */
    public static class Basic<T> extends ApplicationEvent<T, T, Object> {
        public Basic(String name, T... invokerGetter) {
            super(name, invokerGetter);
        }

        @Override
        protected void callWithInvokerParams(T toCall, Object[] params, Object additionalData) {
            try {
                Method method = toCall.getClass().getMethods()[0];
                method.setAccessible(true);
                method.invoke(toCall, params);
            } catch (IllegalAccessException | InvocationTargetException e) {
                Miapi.LOGGER.error("Failed to call event handler for event '{}'! (Multiple methods in interface?)", name);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * A template for ApplicationEvents which use the same interface for both the invoker and listeners,
     * but also have a simple boolean check with additional data passed in.
     * @param <T> the interface for the invoker and listeners
     * @param <A> the additional data type
     */
    public static abstract class Dynamic<T, A> extends ApplicationEvent<T, T, A> {
        public Dynamic(String name, T... invokerGetter) {
            super(name, invokerGetter);
        }

        @Override
        protected void callWithInvokerParams(T toCall, Object[] params, A additionalData) {
            if (canCall(params, additionalData)) {
                try {
                    Method method = toCall.getClass().getMethods()[0];
                    method.setAccessible(true);
                    method.invoke(toCall, params);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    Miapi.LOGGER.error("Failed to call event handler for event '{}'! (Multiple methods in interface?)", name);
                    throw new RuntimeException(e);
                }
            }
        }

        protected abstract boolean canCall(Object[] params, A additionalData);
    }
}
