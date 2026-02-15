package smartin.miapi.client.model.module.dynamic.verlet;

import org.joml.Quaternionf;

public class ChainSegment {

    public final int a;
    public final int b;
    public final int red = (int) (Math.random() * 255);
    public final int blue = (int) (Math.random() * 255);
    public final int green = (int) (Math.random() * 255);

    public float restLength;

    public float radius = 0.05f;
    public boolean collide = false;

    public Quaternionf rot = new Quaternionf();

    public ChainSegment(int a, int b, float length) {
        this.a = a;
        this.b = b;
        this.restLength = length;
    }

}

