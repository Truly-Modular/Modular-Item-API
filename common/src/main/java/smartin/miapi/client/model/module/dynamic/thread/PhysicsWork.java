package smartin.miapi.client.model.module.dynamic.thread;

public interface PhysicsWork {
    /**
     * Executes one fixed-timestamp update.
     */
    void execute(float delta);
}

