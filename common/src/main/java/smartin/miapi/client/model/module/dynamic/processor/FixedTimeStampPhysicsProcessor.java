
package smartin.miapi.client.model.module.dynamic.processor;

import smartin.miapi.client.model.module.dynamic.SimulationState;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class FixedTimeStampPhysicsProcessor<S extends SimulationState<S>> extends AsyncPhysicsProcessor<S> {
    private final long milliseconds;
    private volatile long lastUsedTimeStamp;
    private final AtomicReference<ScheduledExecutorService> EXECUTOR = new AtomicReference<>();

    public FixedTimeStampPhysicsProcessor(long timesPerSecond) {
        super(new Interpolator.UseLast<>());
        if (timesPerSecond <= 0) {
            throw new IllegalArgumentException("timesPerSecond must be positive");
        }
        milliseconds = Math.max(1, 1000 / timesPerSecond);
        queuePhysicsUpdate();
    }


    private void processPhysics() {
        if (isNotInitialized()) {
            return;
        }

        PhysicsFrame<S> frame = scedulesPhysicsFrame.getAndSet(null);

        if (frame == null) {
            if (System.currentTimeMillis() - lastUsedTimeStamp > 1000 * 10) {
                ScheduledExecutorService executorService = EXECUTOR.getAndSet(null);
                if (executorService != null) {
                    executorService.shutdown();
                }
            }
            return;
        }
        lastUsedTimeStamp = System.currentTimeMillis();
        S newState = frame.lastState();
        submitNextRenderState(new RenderFrame<>(frame.beforeTimeStamp() + frame.delta(), frame.handPos(), newState));
    }

    @Override
    void queuePhysicsUpdate() {
        EXECUTOR.getAndUpdate(old -> {
            if (old != null) {
                return old;
            }
            lastUsedTimeStamp = System.currentTimeMillis();
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(this::processPhysics, milliseconds, milliseconds, TimeUnit.MILLISECONDS);
            return executorService;
        });
    }
}
