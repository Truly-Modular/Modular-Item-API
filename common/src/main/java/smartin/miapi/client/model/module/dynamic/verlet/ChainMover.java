package smartin.miapi.client.model.module.dynamic.verlet;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import smartin.miapi.client.model.module.dynamic.ChainState;
import smartin.miapi.client.model.module.dynamic.MatrixHelper;

public class ChainMover {
    public static void teleportChain(ChainSimulationState sim, Vector3f translation) {
        for (int i = 0; i < sim.nodes.length; i++) {
            ChainNode node = sim.nodes[i];

            if (node.locked || node.inverseMass <= 0.0f) {
                continue;
            }

            Vector3f offset = new Vector3f(translation);

            // Reduce translation for nodes touching the floor.
            if (node.collide && node.hadCollision) {
                offset.mul(0.5f);
            }

            node.pos.add(offset);
            node.prevPos.add(offset);
        }

        sim.handlePos.add(translation);
        sim.prevHandlePos.add(translation);
    }


    public static void resolveTransforms(Matrix4f rootMatrix, ChainState state) {
        Vector3f root = MatrixHelper.translateVectorToLocalSpace(state.sim.nodes[0].pos, state.cameraPose, rootMatrix);

        Vector3f previousForward = null;
        Vector3f up = new Vector3f(0, 1, 0);

        for (int i = 0; i < state.sim.segments.length; i++) {

            Vector3f a = MatrixHelper.translateVectorToLocalSpace(state.sim.nodes[i].pos, state.cameraPose, rootMatrix);
            Vector3f b = MatrixHelper.translateVectorToLocalSpace(state.sim.nodes[i + 1].pos, state.cameraPose, rootMatrix);
            Vector3f delta = new Vector3f(b).sub(a);
            float len = delta.length();

            if (len < 1e-6f) {
                continue;
            }

            Vector3f forward = delta.div(len);

            if (previousForward == null) {
                Vector3f reference = Math.abs(forward.y) < 0.9f ? new Vector3f(0, 1, 0) : new Vector3f(1, 0, 0);

                up.set(reference).fma(-reference.dot(forward), forward).normalize();

            } else {
                Quaternionf rotation = new Quaternionf().rotationTo(previousForward, forward);

                rotation.transform(up);

                up.fma(-up.dot(forward), forward).normalize();
            }

            Vector3f right = new Vector3f(up).cross(forward).normalize();

            up.set(forward).cross(right).normalize();

            previousForward = new Vector3f(forward);

            Matrix4f basis = new Matrix4f(right.x, right.y, right.z, 0, up.x, up.y, up.z, 0, forward.x, forward.y, forward.z, 0, 0, 0, 0, 1);

            float targetLength = state.sim.segments[i].desiredLength;
            float sideLength = targetLength + (targetLength - len) / 10f;
            state.linkTransforms[i].identity().translation(new Vector3f(a).sub(root)).mul(basis).scale(sideLength, sideLength, len);
        }
    }
}
