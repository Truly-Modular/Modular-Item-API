package smartin.miapi.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class ObjectUVVertexConsumer implements VertexConsumer {
    public VertexConsumer delegate;
    public Matrix4f objectSpaceInverse;
    public float lastX;
    public float lastY;
    public float lastZ;
    float newU = 0.5f;
    float newV = 0.5f;
    public boolean packUVS = false;


    public ObjectUVVertexConsumer(VertexConsumer delegate, Matrix4f objectSpace, boolean pack, float scale) {
        setup(delegate, objectSpace, scale);
        packUVS = pack;
    }

    public void setup(VertexConsumer delegate, Matrix4f objectSpace, float scale) {
        this.delegate = delegate;
        objectSpaceInverse = new Matrix4f(objectSpace).invert();
        objectSpaceInverse.scale(scale);
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        lastX = x;
        lastY = y;
        lastZ = z;
        Matrix4f inverse = objectSpaceInverse;

        Vector4f local = new Vector4f(lastX, lastY, lastZ, 1.0f);
        inverse.transform(local);

        float scale = 0.0078125f * 100f; // = 0.78125

        newU = -local.x * scale * 2.0f + 0.5f;
        newV = -local.y * scale * 2.0f + 0.5f;
        return delegate.addVertex(x, y, z);
    }


    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        return delegate.setColor(red, green, blue, alpha);
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        if (!packUVS) {
            return delegate.setUv(newU, newV);
        }
        return delegate.setUv(u, v);
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        return delegate.setUv1(u, v);
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        return delegate.setUv2(u, v);
    }

    @Override
    public VertexConsumer setOverlay(int v) {
        return delegate.setOverlay(v);
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        if (packUVS) {
            return delegate.setNormal(newU, newV, z);
        }
        return delegate.setNormal(x, y, z);
    }
}
