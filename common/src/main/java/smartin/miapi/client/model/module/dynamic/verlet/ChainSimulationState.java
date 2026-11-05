
package smartin.miapi.client.model.module.dynamic.verlet;

import org.joml.Vector3f;

public class ChainSimulationState {

    public ChainNode[] nodes;
    public ChainSegment[] segments;

    public AngleConstraint[] angleConstraints =
            new AngleConstraint[0];

    public float prevDt = 0.0f;

    public Vector3f handlePos = new Vector3f();
    public Vector3f prevHandlePos = new Vector3f();

    public final Vector3f prevRootDelta = new Vector3f();
    public final Vector3f rootDelta = new Vector3f();
    public final Vector3f rootAcceleration = new Vector3f();

    public void setAngleConstraints(
            AngleConstraint... constraints
    ) {
        this.angleConstraints = constraints;
    }

    public void resetAngleConstraints() {
        for (AngleConstraint constraint : angleConstraints) {
            constraint.reset();
        }
    }
}
