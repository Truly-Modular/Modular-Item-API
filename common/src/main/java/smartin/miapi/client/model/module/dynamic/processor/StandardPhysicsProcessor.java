package smartin.miapi.client.model.module.dynamic.processor;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import oshi.annotation.concurrent.NotThreadSafe;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.module.dynamic.DynamicModel;
import smartin.miapi.client.model.module.dynamic.SimulationState;

public interface StandardPhysicsProcessor<S extends SimulationState<S>> {
    /**
     * Called on the main thread.
     * Capture inputs required for physics.
     */
    @NotThreadSafe
    void initFrame(Vec3 camPos,
                   Vector3f down,
                   MiapiModel.RenderContext context,
                   double currentTick,
                   DynamicModel<S> model);

    /**
     * Called on the main thread.
     * Capture inputs required for physics.
     */
    @NotThreadSafe
    void beginFrame(Vec3 camPos,
                    Vector3f down,
                    MiapiModel.RenderContext context,
                    double currentTick,
                    DynamicModel<S> model);

    /**
     * Called on the main thread.
     * Returns the state used for rendering.
     */
    @NotThreadSafe
    S getRenderState(double currentTick);

    @NotThreadSafe
    default S getRenderState(Vec3 camPos,
                             Vector3f down,
                             MiapiModel.RenderContext context,
                             double currentTick,
                             DynamicModel<S> model) {
        beginFrame(camPos, down, context, currentTick, model);
        return getRenderState(currentTick);
    }
}
