package smartin.miapi.client.model.module.dynamic.verlet;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import static smartin.miapi.client.model.module.dynamic.ChainCollisionUtil.collideNodeWithWorld;

public class OldVerletIntegrator {

    private static final float EPSILON = 1e-6f;
    private static final float MIN_DT = 1e-4f;
    private static final float MAX_DT = 1.0f / 15.0f;
    private static final float VELOCITY_DAMP = 0.2f;

    public static void runVerlet(
            Level level,
            boolean initSolve,
            float delta,
            float damping,
            ChainSimulationState sim,
            Vector3f gravity,
            int passes,
            boolean ui,
            Vec3 camPos
    ) {
        float dt = clamp(delta, MIN_DT, MAX_DT);
        if(initSolve){
            if (sim.prevDt <= 0.0f) {
                sim.prevDt = dt;
            }
            solveInitVelocity(sim, damping, dt, gravity);
        }

        for (int i = 0; i < passes; i++) {
            boolean stable = integrate(sim, level, ui);
            if (stable) break;
        }

        sim.prevDt = dt;
    }

    private static void solveInitVelocity(
            ChainSimulationState s,
            float damping,
            float dt,
            Vector3f gravity
    ) {
        float dt2 = dt * dt;

        // variable timestep correction
        float dtRatio = dt / s.prevDt;

        for (ChainNode n : s.nodes) {
            if (n.locked) continue;

            float resistance = n.hadCollision ? n.resistance : 1.0f;

            Vector3f current = new Vector3f(n.pos);

            // inferred velocity
            Vector3f vel = new Vector3f(n.pos)
                    .sub(n.prevPos)
                    .mul(dtRatio)
                    .mul(damping * resistance);

            // verlet integration
            n.pos.add(vel);

            n.pos.fma(
                    dt2,
                    new Vector3f(gravity)
                            .mul(n.gravity * resistance)
            );

            n.prevPos.set(current);

            n.hadCollision = false;
        }
    }

    private static boolean integrate(
            ChainSimulationState s,
            Level level,
            boolean ui
    ) {
        s.nodes[0].pos.set(s.handlePos);

        boolean stable = true;

        for (ChainSegment seg : s.segments) {

            ChainNode na = s.nodes[seg.a];
            ChainNode nb = s.nodes[seg.b];

            Vector3f delta = new Vector3f(nb.pos).sub(na.pos);

            float distSq = delta.lengthSquared();

            float dist = (float) Math.sqrt(distSq);

            float error = dist - seg.restLength;

            if (Math.abs(error) > EPSILON) {
                //stable = false;
            }

            // normalized correction
            float correction = error / dist;

            delta.mul(correction);

            if (na.locked && !nb.locked) {
                nb.pos.sub(delta);
                //nb.prevPos.lerp(nb.pos, VELOCITY_DAMP);
            } else if (!na.locked && nb.locked) {
                na.pos.add(delta);
                //na.prevPos.lerp(na.pos, VELOCITY_DAMP);
            } else if (!na.locked) {
                delta.mul(0.5f);
                na.pos.add(delta);
                nb.pos.sub(delta);
                //nb.prevPos.lerp(nb.pos, VELOCITY_DAMP);
                //na.prevPos.lerp(na.pos, VELOCITY_DAMP);
            }

            // collisions
            if (!ui) {

                if (na.collide && !na.locked) {
                    na.hadCollision |=
                            collideNodeWithWorld(level, na.pos, seg.radius);
                }

                if (nb.collide && !nb.locked) {
                    nb.hadCollision |=
                            collideNodeWithWorld(level, nb.pos, seg.radius);
                }
            }
        }

        return stable;
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}