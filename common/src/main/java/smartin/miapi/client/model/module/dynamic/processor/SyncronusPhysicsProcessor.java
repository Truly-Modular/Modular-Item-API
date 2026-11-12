package smartin.miapi.client.model.module.dynamic.processor;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.module.dynamic.DynamicModel;
import smartin.miapi.client.model.module.dynamic.SimulationState;

public class SyncronusPhysicsProcessor<S extends SimulationState<S>> implements PhysicsProcessor<S> {
    S state;
    double lastTick;

    public void initFrame(Vec3 camPos, Vector3f down, MiapiModel.RenderContext context, double currentTick, DynamicModel<S> model) {
        state = model.createState(camPos, context, down);
        lastTick = currentTick;
    }

    @Override
    public S getRenderState(Vec3 camPos, Vector3f down, boolean worldCollision, MiapiModel.RenderContext context, double currentTick, DynamicModel<S> model) {
        if (state == null) {
            initFrame(camPos, down, context, currentTick, model);
        }
        double delta = currentTick - lastTick;
        if (delta > 0) {
            state = model.updatePhysics(state, context.entity(), delta, context.matrices(), down, context.transformationMode());
        }
        return state;
    }
}
