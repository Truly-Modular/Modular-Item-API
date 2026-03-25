package smartin.miapi.client.model.module.dynamic.verlet;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static smartin.miapi.client.model.module.dynamic.ChainCollisionUtil.collideNodeWithWorld;

/**
 * attempt at a verlet implementation, there still seem to be some issues with this
 * TODO:fixme
 */
public class VerletIntegrator {

    public static void runVerlet(Level level, float delta, ChainSimulationState sim, Vector3f gravity, int passes, boolean ui, Vec3 camPos) {
        // constraint passes
        VerletIntegrator.solveInitVelocity(sim, delta, gravity, ui);

        for (int i = 0; i < passes; i++) {
            boolean stable = VerletIntegrator.integrate(sim, level,false, ui);
            if (stable) break;
        }
        //VerletIntegrator.integrate(sim, level,true, ui);
    }

    private static void solveInitVelocity(
            ChainSimulationState s,
            float dt,
            Vector3f gravity,
            boolean ui
    ) {
        float dt2 = dt * dt;

        // --- positional verlet ---
        for (ChainNode n : s.nodes) {
            if (n.locked) continue;

            float damping = 0.98f;
            float resistance = n.hadCollision ? n.resistance : 1.0f;

            Vector3f vel = new Vector3f(n.pos).sub(n.prevPos)
                    .mul(damping * resistance);

            n.prevPos.set(n.pos);
            n.pos.add(vel);
            n.pos.fma(dt2, new Vector3f(gravity).mul(n.gravity * resistance));
            n.hadCollision = false;
        }
    }


    private static boolean integrate(
            ChainSimulationState s,
            Level level,
            boolean fixSolve,
            boolean ui
    ) {
        s.nodes[0].pos.set(s.handlePos);

        boolean notWorked = true;

        for (ChainSegment seg : s.segments) {

            ChainNode na = s.nodes[seg.a];
            ChainNode nb = s.nodes[seg.b];

            // --- positional constraint ---
            Vector3f delta = new Vector3f(nb.pos).sub(na.pos);
            float dist = delta.length();
            if (dist < 1e-6f) continue;
            notWorked = false;

            float diff = (dist - seg.restLength) / dist;
            delta.mul(diff);
            if (fixSolve) {
                if (!nb.locked) {
                    nb.pos.sub(delta);
                }
            } else {
                if (na.locked && !nb.locked) {
                    nb.pos.sub(delta);
                } else if (!na.locked && nb.locked) {
                    na.pos.add(delta);
                } else if (!na.locked) {
                    delta.mul(0.5f);
                    na.pos.add(delta);
                    nb.pos.sub(delta);
                }
            }


            // --- collision ---
            if (!ui) {
                if (na.collide && !na.locked)
                    na.hadCollision |= collideNodeWithWorld(level, na.pos, seg.radius);

                if (nb.collide && !nb.locked)
                    nb.hadCollision |= collideNodeWithWorld(level, nb.pos, seg.radius);
            }
        }
        return notWorked || true;
    }

    public static void computeSegmentRotations(
            ChainNode[] nodes,
            ChainSegment[] segments,
            Quaternionf baseRotation
    ) {
        int n = segments.length;

        Vector3f[] t = new Vector3f[n];
        Vector3f[] normal = new Vector3f[n];

        // tangents
        for (int i = 0; i < n; i++) {
            ChainSegment s = segments[i];
            t[i] = new Vector3f(nodes[s.b].pos)
                    .sub(nodes[s.a].pos)
                    .normalize();
        }

        // initial normal
        Vector3f up = Math.abs(t[0].y) < 0.99f
                ? new Vector3f(0, 1, 0)
                : new Vector3f(1, 0, 0);

        normal[0] = up.cross(t[0], new Vector3f()).normalize();

        // first frame
        {
            Quaternionf frameRot = new Quaternionf();
            setRotationFromFrame(frameRot, t[0], normal[0]);
            segments[0].rot.set(baseRotation).mul(frameRot);
        }

        // parallel transport
        for (int i = 0; i < n - 1; i++) {
            Vector3f v = t[i].cross(t[i + 1], new Vector3f());
            float c = t[i].dot(t[i + 1]);

            if (v.lengthSquared() < 1e-6f) {
                normal[i + 1] = new Vector3f(normal[i]);
            } else {
                float angle = (float) Math.atan2(v.length(), c);
                v.normalize();
                normal[i + 1] = new Vector3f(normal[i])
                        .rotateAxis(angle, v.x, v.y, v.z);
            }

            Quaternionf frameRot = new Quaternionf();
            setRotationFromFrame(frameRot, t[i + 1], normal[i + 1]);

            segments[i + 1].rot.set(baseRotation).mul(frameRot);
        }
    }


    // Z = tangent, X = normal, Y = binormal
    private static void setRotationFromFrame(
            Quaternionf out,
            Vector3f tangent,
            Vector3f normal
    ) {
        Vector3f binormal = tangent.cross(normal, new Vector3f());

        Matrix3f m = new Matrix3f(
                normal.x, binormal.x, tangent.x,
                normal.y, binormal.y, tangent.y,
                normal.z, binormal.z, tangent.z
        );

        out.setFromUnnormalized(m);
    }
}

