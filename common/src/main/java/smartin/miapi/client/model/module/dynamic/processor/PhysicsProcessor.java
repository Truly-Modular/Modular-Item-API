package smartin.miapi.client.model.module.dynamic.processor;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import oshi.annotation.concurrent.NotThreadSafe;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.module.dynamic.DynamicModel;
import smartin.miapi.client.model.module.dynamic.SimulationState;

public interface PhysicsProcessor<S extends SimulationState<S>> {

    @NotThreadSafe
    S getRenderState(Vec3 camPos,
                     Vector3f down,
                     boolean worldCollision,
                     MiapiModel.RenderContext context,
                     double currentTick,
                     DynamicModel<S> model);
}