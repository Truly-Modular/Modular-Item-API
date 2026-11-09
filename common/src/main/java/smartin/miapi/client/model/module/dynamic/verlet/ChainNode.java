
package smartin.miapi.client.model.module.dynamic.verlet;

import org.joml.Vector3f;

public class ChainNode {

    public final Vector3f pos = new Vector3f();
    public final Vector3f prevPos = new Vector3f();

    public final Vector3f force = new Vector3f();

    public boolean locked = false;
    public boolean collide = false;
    public boolean entityCollide = false;

    public float gravity = 1.0f;
    public float resistance = 1.0f;
    public boolean hadCollision = false;

    // Zero means infinite mass.
    public float inverseMass = 1.0f;

    public void addForce(Vector3f value) {
        if (!locked && inverseMass > 0.0f) {
            force.add(value);
        }
    }

    public void addAcceleration(Vector3f value) {
        if (!locked && inverseMass > 0.0f) {
            force.fma(1.0f / inverseMass, value);
        }
    }

    public void clearForces() {
        force.zero();
    }

    public void setMass(float mass) {
        inverseMass = mass <= 0.0f ? 0.0f : 1.0f / mass;
    }

    public void lock() {
        locked = true;
        inverseMass = 0.0f;
    }

    public void unlock() {
        locked = false;
        if (inverseMass == 0.0f) {
            inverseMass = 1.0f;
        }
    }
}
