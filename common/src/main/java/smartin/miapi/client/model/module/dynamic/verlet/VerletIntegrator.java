package smartin.miapi.client.model.module.dynamic.verlet;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

import static smartin.miapi.client.model.module.dynamic.ChainCollisionUtil.collideNodeWithEntities;
import static smartin.miapi.client.model.module.dynamic.ChainCollisionUtil.collideNodeWithWorld;

public class VerletIntegrator {

    private static final float EPSILON = 1e-6f;
    private static final float MIN_DIST_SQ = 1e-12f;

    private static final float FIXED_DT = 1.0f / 60.0f;
    private static final float MAX_DT = 1.0f;
    private static final int MAX_STEPS = 10;

    public static void runVerlet(Level level, List<ChainUpdater> updaters, int minSteps, boolean initSolve, float delta, float damping, float rigidness, ChainSimulationState sim, Vector3f gravity, int passes, boolean ui, Vec3 camPos, Entity noCollide) {
        float dt = Math.max(1e-4f, Math.min(delta, MAX_DT));
        int steps = Math.min(MAX_STEPS,Math.max(1, Math.max(minSteps, (int) Math.ceil(dt / FIXED_DT))));
        float h = dt / steps;
        float deltaPercent = 0.0f;
        /*
        i should explain this mess a bit
        to stabilize the verlet a bit through changing delta times,
        i figured i divide each movement into steps with a close to fixed Delta Time instead.

         */

        for (int i = 0; i < steps; i++) {
            deltaPercent += 1.0f / steps;
            for (ChainUpdater updater : updaters) {
                updater.apply(sim, deltaPercent);
            }

            Vector3f oldHandle = new Vector3f(sim.prevHandlePos);
            sim.rootDelta.set(sim.handlePos).sub(oldHandle);
            sim.rootAcceleration.set(sim.rootDelta).sub(sim.prevRootDelta);
            sim.prevRootDelta.set(sim.rootDelta);
            if (initSolve) {
                integrateMotion(sim, h, damping, gravity);
            }

            sim.resetAngleConstraints();
            for (int p = 0; p < passes; p++) {
                solveConstraints(sim, level, rigidness, h, ui, noCollide);
            }
            sim.prevDt = h;
        }
    }

    private static void integrateMotion(ChainSimulationState s, float dt, float damping, Vector3f gravity) {
        float dt2 = dt * dt;
        float normalizedDamping = (float) Math.pow(damping, dt / FIXED_DT);
        for (ChainNode n : s.nodes) {
            if (n.locked || n.inverseMass <= 0.0f) {
                n.clearForces();
                continue;
            }

            Vector3f current = new Vector3f(n.pos);
            float dtRatio = s.prevDt > 0.0f ? dt / s.prevDt : 1.0f;
            Vector3f velocity = new Vector3f(n.pos).sub(n.prevPos).mul(dtRatio).mul(normalizedDamping);
            n.pos.add(velocity);

            float resistance = n.hadCollision ? n.resistance : 1.0f;
            Vector3f acceleration = new Vector3f(gravity).mul(n.gravity * resistance);
            acceleration.fma(n.inverseMass, n.force);

            n.pos.fma(dt2, acceleration);
            n.prevPos.set(current);
            n.hadCollision = false;
            n.clearForces();
        }
    }

    private static void solveConstraints(ChainSimulationState s, Level level, float rigidness, float dt, boolean ui, Entity noCollide) {
        float stiffness = 1.0f - Math.max(0.0f, Math.min(1.0f, rigidness));
        float stiffnessPerStep = 1.0f - (float) Math.pow(1.0f - stiffness, dt / FIXED_DT);
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

            delta.mul(error / dist);

            float wA = (a.locked || seg.a == 0) ? 0.0f : a.inverseMass;
            float wB = (b.locked || seg.b == 0) ? 0.0f : b.inverseMass;
            float wSum = wA + wB;

            if (wSum <= 0.0f) {
                continue;
            }

            if (wA > 0.0f) {
                a.pos.fma((wA / wSum) * stiffnessPerStep, delta);
            }

            if (wB > 0.0f) {
                b.pos.fma(-(wB / wSum) * stiffnessPerStep, delta);
            }
        }
        for (AngleConstraint constraint : s.angleConstraints) {
            constraint.solve(s, dt, true);
        }
        if (!ui) {
            for (ChainSegment seg : s.segments) {
                ChainNode a = s.nodes[seg.a];
                ChainNode b = s.nodes[seg.b];

                if (a.collide && !a.locked && seg.a != 0) {
                    a.hadCollision |= collideNodeWithWorld(level, a.pos, seg.radius);
                }

                if (b.collide && !b.locked && seg.b != 0) {
                    b.hadCollision |= collideNodeWithWorld(level, b.pos, seg.radius);
                }

                if (a.entityCollide && !a.locked && seg.a != 0) {
                    a.hadCollision |= collideNodeWithEntities(
                            level, noCollide, a.pos, seg.radius
                    );
                }

                if (b.entityCollide && !b.locked && seg.b != 0) {
                    b.hadCollision |= collideNodeWithEntities(
                            level, noCollide, b.pos, seg.radius
                    );
                }
            }
        }
        s.nodes[0].pos.set(s.handlePos);
    }

    public interface ChainUpdater {
        void apply(ChainSimulationState sim, float deltaPercent);
    }
}
