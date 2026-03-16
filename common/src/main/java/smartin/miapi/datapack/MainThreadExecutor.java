package smartin.miapi.datapack;

import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

public class MainThreadExecutor implements Executor {

    private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();

    @Override
    public void execute(@NotNull Runnable command) {
        queue.add(command);
    }

    public void drain() {
        Runnable task;
        while ((task = queue.poll()) != null) {
            task.run();
        }
    }
}