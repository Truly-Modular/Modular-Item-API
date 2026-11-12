package smartin.miapi.client.model.module.dynamic.processor;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import oshi.annotation.concurrent.NotThreadSafe;
import oshi.annotation.concurrent.ThreadSafe;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.module.dynamic.DynamicModel;
import smartin.miapi.client.model.module.dynamic.SimulationState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class just creates the barebones logic to allow for proper threaded work.
 * Any implementation of this needs to customly cleyr {@link AsyncPhysicsProcessor#scedulesPhysicsFrame} manually, this can happen of thread.
 * it then needs to supply a new Frame in {@link AsyncPhysicsProcessor#scedulesPhysicsFrame} to be rendered.
 */
public abstract class AsyncPhysicsProcessor<S extends SimulationState<S>> implements PhysicsProcessor<S> {
    protected final AtomicReference<PhysicsFrame<S>> scedulesPhysicsFrame = new AtomicReference<>();
    private final AtomicReference<List<RenderFrame<S>>> renderFrames = new AtomicReference<>(List.of());
    private final int maxHistoryFrames = 64;
    protected double lastTick;
    protected Interpolator<S> interpolator;

    protected AsyncPhysicsProcessor(Interpolator<S> interpolator) {
        this.interpolator = interpolator;
    }

    @NotThreadSafe
    public void initFrame(Vec3 camPos, Vector3f down, MiapiModel.RenderContext context, double currentTick, DynamicModel<S> model) {
        S initialState = model.createState(camPos, context, down);
        RenderFrame<S> initFrame = new RenderFrame<>(currentTick, new Vector3f(), initialState);
        renderFrames.set(List.of(initFrame, initFrame));
        lastTick = currentTick;
    }

    @NotThreadSafe
    abstract void queuePhysicsUpdate();

    @ThreadSafe
    public void submitNextRenderState(RenderFrame<S> nextState) {
        renderFrames.updateAndGet(existing -> {
            List<RenderFrame<S>> updated = new ArrayList<>(existing);

            int index = Collections.binarySearch(
                    updated,
                    nextState,
                    Comparator.comparingDouble(RenderFrame<S>::timeStamp)
            );

            if (index >= 0) {
                updated.set(index, nextState);
            } else {
                updated.add(-index - 1, nextState);
            }

            while (updated.size() > maxHistoryFrames) {
                updated.remove(0);
            }

            return List.copyOf(updated);
        });
    }

    /**
     * this collects the Physics frame.
     * This collection SHOULD be processed and the result should call {@link AsyncPhysicsProcessor#submitNextRenderState(RenderFrame)}
     */
    @ThreadSafe
    @Nullable
    public PhysicsFrame<S> collectPhysicsFrame() {
        return scedulesPhysicsFrame.getAndSet(null);
    }

    /**
     * should only be called by the main thread
     * informs the {@link AsyncPhysicsProcessor#scedulesPhysicsFrame} if a new physics state change has happened.
     * if the last update hasn't been collected by the processor, they are being merged.
     *
     * @return
     */
    @Override
    @NotThreadSafe
    public S getRenderState(Vec3 camPos, Vector3f down, boolean worldCollision, MiapiModel.RenderContext context, double currentTick, DynamicModel<S> model) {
        if (isNotInitialized()) {
            initFrame(camPos, down, context, currentTick, model);
        }
        Vector3f start = new Vector3f();
        double delta = currentTick - lastTick;
        if (delta > 0) {
            lastTick = currentTick;
            scedulesPhysicsFrame.getAndUpdate((existing) -> {
                if (existing == null) {
                    return new PhysicsFrame<>(
                            currentTick - delta,
                            delta,
                            start,
                            camPos,
                            new Vector3f(down),
                            renderFrames.get().getLast().state());
                }
                return new PhysicsFrame<>(
                        existing.beforeTimeStamp(),
                        existing.delta() + delta,
                        start,
                        camPos,
                        new Vector3f(down),
                        existing.lastState());
            });
            queuePhysicsUpdate();
        }
        List<RenderFrame<S>> nextRender = renderFrames.get();
        if (currentTick != nextRender.getLast().timeStamp()) {
            return interpolate(nextRender, start, currentTick);
        }
        return nextRender.getLast().state();
    }

    @NotThreadSafe
    public S interpolate(List<RenderFrame<S>> lastComputed, Vector3f currentStart, double deltaTick) {
        return interpolator.interpolate(lastComputed, currentStart, deltaTick);
    }

    @NotThreadSafe
    protected boolean isNotInitialized() {
        return renderFrames.get().isEmpty();
    }

    public record PhysicsFrame<S>(
            double beforeTimeStamp,
            double delta,
            Vector3f handPos,
            Vec3 cameraPos,
            Vector3f gravity,
            S lastState) {}

    public record RenderFrame<S>(
            double timeStamp,
            Vector3f handPos,
            S state) {}

    public record RenderFrames<S>(
            RenderFrame<S> current,
            RenderFrame<S> last) {}
}
