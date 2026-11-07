package smartin.miapi.client.model.module.dynamic.verlet;

import org.joml.Vector3f;

public class ChainMover {
    public static void teleportChain(
            ChainSimulationState sim,
            Vector3f translation
    ) {
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
}
