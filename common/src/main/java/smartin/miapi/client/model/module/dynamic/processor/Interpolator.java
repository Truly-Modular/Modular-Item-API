package smartin.miapi.client.model.module.dynamic.processor;

import org.joml.Vector3f;
import smartin.miapi.client.model.module.dynamic.SimulationState;

import java.util.List;

public interface Interpolator<S extends SimulationState<S>> {
    S interpolate(List<AsyncPhysicsProcessor.RenderFrame<S>> lastComputed, Vector3f currentStart, double deltaTick);

    record UseLast<T extends SimulationState<T>>() implements Interpolator<T> {
        @Override
        public T interpolate(List<AsyncPhysicsProcessor.RenderFrame<T>> lastComputed, Vector3f currentStart, double deltaTick) {
            return lastComputed.getLast().state();
        }
    }
}
