package smartin.miapi.client.model.module.dynamic.verlet;

import org.joml.Vector3f;

public class ChainSimulationState {

    public ChainNode[] nodes;
    public ChainSegment[] segments;

    public Vector3f handlePos = new Vector3f();
    public Vector3f prevHandlePos = new Vector3f();
}
