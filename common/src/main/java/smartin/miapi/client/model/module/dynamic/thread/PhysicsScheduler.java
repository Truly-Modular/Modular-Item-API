package smartin.miapi.client.model.module.dynamic.thread;

import java.util.concurrent.*;

/**
 * Sceduler class to run off-thread client side physics sims or other work intensive rendering things.
 * currently there seem to be issues with the delta calculation.
 * should like also try to capture tick rate
 */
public final class PhysicsScheduler {

    private static final ConcurrentLinkedQueue<PhysicsWork> queue =
            new ConcurrentLinkedQueue<>();

    private static final ConcurrentHashMap<PhysicsWork, Boolean> inFlight =
            new ConcurrentHashMap<>();

    private static final int WORKER_COUNT =
            Math.max(1, Runtime.getRuntime().availableProcessors() - 1);

    private static final ExecutorService workers =
            Executors.newFixedThreadPool(WORKER_COUNT, r -> {
                Thread t = new Thread(r, "Physics-Worker");
                t.setDaemon(true);
                return t;
            });

    private static final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "Physics-Scheduler");
                t.setDaemon(true);
                return t;
            });

    private static volatile ScheduledFuture<?> scheduledTask;
    private static volatile long updatePeriodNanos =
            TimeUnit.SECONDS.toNanos(1) / 60;

    private PhysicsScheduler() {
    }

    /* ------------------------------------------------------------ */

    public static void start() {
        reschedule();
    }

    public static synchronized void setUpdatesPerSecond(int updatesPerSecond) {
        if (updatesPerSecond <= 0)
            throw new IllegalArgumentException("updatesPerSecond must be > 0");

        updatePeriodNanos =
                TimeUnit.SECONDS.toNanos(1) / updatesPerSecond;

        reschedule();
    }

    public static void submit(PhysicsWork work) {
        if (inFlight.putIfAbsent(work, Boolean.TRUE) == null) {
            queue.add(work);
        }
    }

    private static synchronized void reschedule() {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }

        scheduledTask = scheduler.scheduleAtFixedRate(
                PhysicsScheduler::dispatch,
                0,
                updatePeriodNanos,
                TimeUnit.NANOSECONDS
        );
    }

    private static void dispatch() {
        PhysicsWork work;

        while ((work = queue.poll()) != null) {
            PhysicsWork task = work;

            workers.execute(() -> {
                try {
                    task.execute(1000000.0f / updatePeriodNanos);
                } catch (Throwable t) {
                    t.printStackTrace();
                } finally {
                    inFlight.remove(task);
                }
            });
        }
    }
}
