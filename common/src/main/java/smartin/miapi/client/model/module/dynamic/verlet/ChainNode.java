package smartin.miapi.client.model.module.dynamic.verlet;

import org.joml.Vector3f;

public class ChainNode {

    public final Vector3f pos = new Vector3f();
    public final Vector3f prevPos = new Vector3f();

    public boolean locked = false;
    public boolean collide = false;
    public float gravity = 1.0f;
    public float resistance = 0.3f;
    public boolean hadCollision = false;
}
