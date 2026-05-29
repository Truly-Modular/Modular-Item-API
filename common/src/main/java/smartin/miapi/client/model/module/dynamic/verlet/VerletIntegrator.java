package smartin.miapi.client.model.module.dynamic.verlet;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import smartin.miapi.Miapi;

import java.util.List;

import static smartin.miapi.client.model.module.dynamic.ChainCollisionUtil.collideNodeWithWorld;

public class VerletIntegrator {

    private static final float EPSILON = 1e-6f;
    private static final float MIN_DIST_SQ = 1e-12f;

    private static final float FIXED_DT = 1.0f / 60.0f;
    private static final float MAX_DT = 1.0f / 15.0f;

    private static float r(float v) {
        return Math.round(v * 1000f) / 1000f;
    }


    public static void runVerlet(
            Level level,
            List<ChainUpdater> updaters,
            int minSteps,
            boolean initSolve,
            float delta,
            float damping,
            //probably should think of a better name for this
            float rigidness, // 0 = rigid, 1 = springy
            ChainSimulationState sim,
            Vector3f gravity,
            int passes,
            boolean ui,
            Vec3 camPos
    ) {

        float dt = Math.max(1e-4f, Math.min(delta, MAX_DT));

        //sth still si wrong with running multiple steps
        //cant figure it out, even loging vel and accel doesnt seem to imply some rapid changes there,
        //but chain still behaves incredibly weirdly
        int steps = 1;//Math.max(minSteps, (int) Math.ceil(dt / FIXED_DT));
        float h = dt / steps;
        float deltaPercent = 0;
        for (int i = 0; i < steps; i++) {

            deltaPercent += 1.0f / steps;
            for (ChainUpdater updater : updaters) {
                updater.apply(sim, deltaPercent);
            }
            Vector3f oldHandle = new Vector3f(sim.prevHandlePos);
            sim.rootDelta
                    .set(sim.handlePos)
                    .sub(oldHandle);

            sim.rootAcceleration
                    .set(sim.rootDelta)
                    .sub(sim.prevRootDelta);

            sim.prevRootDelta.set(sim.rootDelta);
            Miapi.LOGGER.info(
                    "vel=" + r(sim.rootDelta.length()) +
                    " accel=" + r(sim.rootAcceleration.length())
            );
            if (initSolve) {
                integrateMotion(sim, h, damping, gravity);
            }

            for (int p = 0; p < passes; p++) {
                solveConstraints(sim, level, rigidness, h, ui);
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

        // timestep-invariant damping
        float normalizedDamping =
                (float) Math.pow(damping, dt / FIXED_DT);

        for (ChainNode n : s.nodes) {

            if (n.locked) {
                continue;
            }

            Vector3f current = new Vector3f(n.pos);

            //corrections for variable delta times
            float dtRatio = s.prevDt > 0f
                    ? dt / s.prevDt
                    : 1f;

            Vector3f vel = new Vector3f(n.pos)
                    .sub(n.prevPos)
                    .mul(dtRatio)
                    .mul(normalizedDamping);

            n.pos.add(vel);

            float resistance =
                    n.hadCollision
                            ? n.resistance
                            : 1.0f;

            n.pos.fma(
                    dt2,
                    new Vector3f(gravity)
                            .mul(n.gravity * resistance)
            );

            n.prevPos.set(current);
            n.hadCollision = false;
        }
    }

    private static void solveConstraints(
            ChainSimulationState s,
            Level level,
            float rigidness,
            float dt,
            boolean ui
    ) {

        float stiffness = 1.0f - Math.max(0f, Math.min(1f, rigidness));
        float stiffnessPerStep =
                1.0f - (float) Math.pow(
                        1.0f - stiffness,
                        dt / FIXED_DT
                );

        s.nodes[0].pos.set(s.handlePos);

        for (ChainSegment seg : s.segments) {

            ChainNode a = s.nodes[seg.a];
            ChainNode b = s.nodes[seg.b];

            Vector3f delta = new Vector3f(b.pos).sub(a.pos);

            float distSq = delta.lengthSquared();

            if (distSq < MIN_DIST_SQ) {
                continue;
            }

            float dist = (float) Math.sqrt(distSq);

            float error = dist - seg.restLength;

            if (Math.abs(error) < EPSILON) {
                continue;
            }

            float invDist = 1.0f / dist;

            delta.mul(invDist * error);

            float wA = a.locked ? 0f : 1f;
            float wB = b.locked ? 0f : 1f;

            float wSum = wA + wB;

            if (wSum <= 0f) {
                continue;
            }

            Vector3f correctionA = new Vector3f(delta)
                    .mul((wA / wSum) * stiffnessPerStep);

            Vector3f correctionB = new Vector3f(delta)
                    .mul((wB / wSum) * stiffnessPerStep);

            if (!a.locked) {
                a.pos.add(correctionA);
            }

            if (!b.locked) {
                b.pos.sub(correctionB);
            }
        }

        if (!ui) {

            for (ChainSegment seg : s.segments) {

                ChainNode a = s.nodes[seg.a];
                ChainNode b = s.nodes[seg.b];

                if (a.collide && !a.locked) {
                    a.hadCollision |=
                            collideNodeWithWorld(
                                    level,
                                    a.pos,
                                    seg.radius
                            );
                }

                if (b.collide && !b.locked) {
                    b.hadCollision |=
                            collideNodeWithWorld(
                                    level,
                                    b.pos,
                                    seg.radius
                            );
                }
            }
        }
    }

    public interface ChainUpdater {
        void apply(ChainSimulationState sim, float deltaPercent);
    }
}