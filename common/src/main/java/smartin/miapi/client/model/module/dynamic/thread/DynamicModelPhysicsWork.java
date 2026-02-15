package smartin.miapi.client.model.module.dynamic.thread;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import smartin.miapi.client.model.module.dynamic.DynamicModel;
import smartin.miapi.client.model.module.dynamic.SimulationState;

/**
 * Manages queued physics updates for {@link DynamicModel}
 */
public record DynamicModelPhysicsWork<S extends SimulationState>(
        DynamicModel<S> model,
        S state,
        ItemDisplayContext context,
        @Nullable LivingEntity entity,
        Vector3f down
) implements PhysicsWork {

    @Override
    public void execute(float delta) {
        model.updatePhysics(
                state,
                entity,
                delta,
                state.pose,
                down,
                context
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DynamicModelPhysicsWork<?> other)) return false;

        return state == other.state
               && context == other.context;
    }

    @Override
    public int hashCode() {
        int result = System.identityHashCode(state);
        result = 31 * result + context.hashCode();
        return result;
    }
}
