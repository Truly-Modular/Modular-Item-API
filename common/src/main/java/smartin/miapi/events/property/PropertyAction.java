package smartin.miapi.events.property;

import com.google.common.base.Suppliers;
import com.google.common.reflect.AbstractInvocationHandler;
import dev.architectury.event.Event;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class PropertyAction<T> {
    public final Event<T> event;
    public final Function<WorldBinder, T> worldBinder;
    public final T returner;
    // public final ContextOutline context; //todo context shtuff

    /**
     * @param event the event this property action reflects
     * @param returner a listener to the event that returns a value to be returned by the actual listener
     * @param worldBinder a listener to the event that must use the passed in WorldBinder to set a world based on listener parameters
     * @param typeGetter used for class getting, always keep this array empty.
     */
    @SafeVarargs
    public PropertyAction(Event<T> event, T returner, Function<WorldBinder, T> worldBinder, T... typeGetter) {
        this(event, returner, worldBinder, Suppliers.memoize(() -> {
            if (typeGetter.length != 0) throw new IllegalStateException("Type getter array for PropertyAction must be empty!");
            return  (Class<T>) typeGetter.getClass().getComponentType();
        }).get()); // used a supplier here to effectively run code before other constructor runs
    }

    public PropertyAction(Event<T> event, T returner, Function<WorldBinder, T> worldBinder, Class<T> cls) {
        this.event = event;
        this.returner = returner;
        this.worldBinder = worldBinder;

        event.register(getEventListener(cls));
    }

    protected void bindWorld(WorldBinder binder, Method method, Object[] methodArgs) throws Throwable {
        invokeMethod(worldBinder.apply(binder), method, methodArgs);
    }

    protected T getEventListener(Class<T> listenerClass) {
        return (T) Proxy.newProxyInstance(PropertyAction.class.getClassLoader(), new Class[]{listenerClass}, new AbstractInvocationHandler() {
            @Override
            protected Object handleInvocation(Object proxy, Method method, @Nullable Object[] args) throws Throwable {
                WorldBinder binder = new WorldBinder();
                bindWorld(binder, method, args);
                World world = binder.validateAndGet();

                PropertyActionManager.activeSlots.forEach((entity, map) -> {
                    if (entity.getWorld() != world) return; // making sure the entity is not in another dimension, for optimization purposes
                    map.forEach((str, stackGetter) -> {
                        ItemStack stack = stackGetter.get();
                        PropertyActionManager.getListeners(stack).get(event).forEach(listener -> {
                            try {
                                invokeMethod(listener, method, args);
                            } catch (Throwable e) {
                                Miapi.LOGGER.error("""
                                                      Exception whilst trying to invoke listener method! (Perhaps the listener doesn't match the event?)
                                                      Listener:\s""" + listener + "\nMethod: " + method + "\nArguments: " + Arrays.toString(args) + "\n... Event: " + event, e);
                            }
                        });
                    });
                });

                return invokeMethod(returner, method, args);
            }
        });
    }

    private static <T, R> R invokeMethod(T listener, Method method, Object[] args) throws Throwable {
        return (R) MethodHandles.lookup().unreflect(method)
                .bindTo(listener).invokeWithArguments(args);
    }

    public static class WorldBinder {
        private World world = null;

        public <T> T setWorld(World world) {
            this.world = world;
            return null;
        } // generics so you can inline this in returns for cleanliness

        protected World validateAndGet() {
            if (world == null) throw new RuntimeException("World not bound for PropertyAction!");
            return world;
        }
    }
}
