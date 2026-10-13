package smartin.miapi.client.model.collision;

import org.joml.Vector3f;

public class TriangleHitHelper {
    public static final float RAY_EPSILON = 1.0e-6f;
    public static final int RAYCAST_ALPHA_THRESHOLD = 255;


    public record TriangleHit(float t, float u, float v) {}

    public static TriangleHit intersectTriangle(Ray ray, Vector3f v0, Vector3f v1, Vector3f v2) {
        Vector3f edge1 = new Vector3f(v1).sub(v0);
        Vector3f edge2 = new Vector3f(v2).sub(v0);
        Vector3f pvec = new Vector3f(ray.direction()).cross(edge2);
        float determinant = edge1.dot(pvec);
        if (Math.abs(determinant) < RAY_EPSILON) {return null;}
        float inverseDeterminant = 1.0f / determinant;
        Vector3f tvec = new Vector3f(ray.origin()).sub(v0);
        float u = tvec.dot(pvec) * inverseDeterminant;
        if (u < 0.0f || u > 1.0f) {return null;}
        Vector3f qvec = new Vector3f(tvec).cross(edge1);
        float v = ray.direction().dot(qvec) * inverseDeterminant;
        if (v < 0.0f || u + v > 1.0f) {return null;}
        float t = edge2.dot(qvec) * inverseDeterminant;
        if (t < RAY_EPSILON) {return null;}
        return new TriangleHit(t, u, v);
    }
}