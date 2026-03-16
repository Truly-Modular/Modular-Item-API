package smartin.miapi.datapack;

import net.minecraft.core.RegistryAccess;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * A class for handling reload events. Instances of this class represent specific stages of the reload process, and
 * can be subscribed to using the {@link #subscribe(ReloadEvents.EventListener)} and {@link #subscribe(ReloadEvents.EventListener, float)} methods.
 * When a reload event is fired using the {@link #fireEvent(boolean, RegistryAccess)} method, the registered listeners will be called in
 * order of their priority (with lower-priority listeners being called first).
 */
public class ReloadEvent {
    private final Map<ReloadEvents.EventListener, Float> mainListeners = new HashMap<>();
    private final ExecutorService loaderService = new ForkJoinPool();

    /**
     * Subscribes the given listener to this reload event, with the given priority. Listeners with lower priorities will
     * be called first when this event is fired.
     *
     * @param listener the listener to subscribe
     * @param priority the priority of the listener
     */
    public void subscribe(ReloadEvents.EventListener listener, float priority) {
        mainListeners.put(listener, priority);
    }

    /**
     * Subscribes the given listener to this reload event, with a default priority of 0. Listeners with lower priorities
     * will be called first when this event is fired.
     *
     * @param listener the listener to subscribe
     */
    public void subscribe(ReloadEvents.EventListener listener) {
        subscribe(listener, 0);
    }

    /**
     * Unsubscribes the given listener from this reload event.
     *
     * @param listener the listener to unsubscribe
     */
    public void unsubscribe(ReloadEvents.EventListener listener) {
        mainListeners.remove(listener);
    }

    /**
     * Fires this reload event, calling all registered listeners in order of their priority (with lower-priority
     * listeners being called first). The {@code isClient} parameter indicates whether the event is occurring on the
     * client side (true) or the server side (false).
     *
     * @param isClient a boolean indicating whether the event is occurring on the client side (true) or the server side (false)
     */
    public void fireEvent(boolean isClient, @Nullable RegistryAccess registryAccess) {
        fireEvent(isClient, registryAccess, loaderService);
    }

    public void fireEvent(boolean isClient, @Nullable RegistryAccess registryAccess, ExecutorService executor) {
        mainListeners.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> {
                    //TODO:paralelize work of same priority
                    List<CompletableFuture<?>> queuedFutures = new ArrayList<>();
                    AtomicBoolean active = new AtomicBoolean(true);

                    Consumer<CompletableFuture<?>> async = future -> {
                        if (!active.get()) {
                            throw new IllegalStateException(
                                    "Async work queued after listener finished"
                            );
                        }

                        // ensure the work runs on your executor
                        CompletableFuture<?> scheduled =
                                future.thenApplyAsync(x -> x, executor);

                        queuedFutures.add(scheduled);
                    };

                    try {
                        entry.getKey().onEvent(isClient, registryAccess, async);
                    } catch (RuntimeException e) {
                        Miapi.LOGGER.error("Exception during reload " + entry.getKey().getClass().getName(), e);
                    }

                    // optimization: skip if no async work
                    if (queuedFutures.isEmpty()) {
                        active.set(false);
                        return;
                    }

                    try {
                        CompletableFuture
                                .allOf(queuedFutures.toArray(new CompletableFuture[0]))
                                .join();
                    } catch (Exception e) {
                        Miapi.LOGGER.error("Async reload task failed", e);
                    } finally {
                        //de-activate ability to use executor service
                        active.set(false);
                    }
                });
    }
}
