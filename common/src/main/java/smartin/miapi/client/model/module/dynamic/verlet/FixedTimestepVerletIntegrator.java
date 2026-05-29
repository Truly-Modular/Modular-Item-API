package smartin.miapi.client.model.module.dynamic.verlet;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import static smartin.miapi.client.model.module.dynamic.ChainCollisionUtil.collideNodeWithWorld;

public class FixedTimestepVerletIntegrator {

    private static final float EPSILON = 1e-5f;
    private static final float FIXED_DT = 1.0f / 60.0f;

    public static void runVerlet(
            Level level,
            boolean initSolve,
            float delta,
            float damping,
            float rigidness, // 0 = rigid, 1 = springy
            ChainSimulationState sim,
            Vector3f gravity,
            int passes,
            boolean ui,
            Vec3 camPos
    ) {
        float dt = Math.max(1e-4f, Math.min(delta, 1.0f / 15.0f));

        int steps = Math.max(1, (int) Math.ceil(dt / FIXED_DT));
        float h = dt / steps;

        for (int i = 0; i < steps; i++) {
            if(initSolve){
                integrateMotion(sim, h, damping, gravity);
            }

            for (int p = 0; p < passes; p++) {
                solveConstraints(sim, level, rigidness, ui);
            }

            sim.prevDt = h;
        }
    }

    private static void integrateMotion(
            ChainSimulationState s,
            float dt,
            float damping,
            Vector3f gravity
    ) {
        float dt2 = dt * dt;

        for (ChainNode n : s.nodes) {

            if (n.locked) continue;

            Vector3f current = new Vector3f(n.pos);

            Vector3f vel = new Vector3f(n.pos).sub(n.prevPos).mul(damping);
            n.pos.add(vel);

            float res = n.hadCollision ? n.resistance : 1.0f;

            n.pos.fma(
                    dt2,
                    new Vector3f(gravity).mul(n.gravity * res)
            );

            n.prevPos.set(current);
            n.hadCollision = false;
        }
    }

    private static void solveConstraints(
            ChainSimulationState s,
            Level level,
            float rigidness,
            boolean ui
    ) {
        float stiffness = 1.0f - Math.max(0f, Math.min(1f, rigidness));

        s.nodes[0].pos.set(s.handlePos);

        for (ChainSegment seg : s.segments) {

            ChainNode a = s.nodes[seg.a];
            ChainNode b = s.nodes[seg.b];

            Vector3f delta = new Vector3f(b.pos).sub(a.pos);

            float distSq = delta.lengthSquared();
            if (distSq < 1e-12f) continue;

            float dist = (float) Math.sqrt(distSq);
            float error = dist - seg.restLength;

            if (Math.abs(error) < EPSILON) continue;

            float invDist = 1.0f / dist;
            delta.mul(invDist * error);

            float wA = a.locked ? 0f : 1f;
            float wB = b.locked ? 0f : 1f;
            float wSum = wA + wB;

            if (wSum == 0f) continue;

            Vector3f correctionA = new Vector3f(delta).mul(wA / wSum).mul(stiffness);
            Vector3f correctionB = new Vector3f(delta).mul(wB / wSum).mul(stiffness);

            if (!a.locked) a.pos.add(correctionA);
            if (!b.locked) b.pos.sub(correctionB);

            if (!ui) {
                if (a.collide && !a.locked) {
                    a.hadCollision |= collideNodeWithWorld(level, a.pos, seg.radius);
                }
                if (b.collide && !b.locked) {
                    b.hadCollision |= collideNodeWithWorld(level, b.pos, seg.radius);
                }
            }
        }
    }
}