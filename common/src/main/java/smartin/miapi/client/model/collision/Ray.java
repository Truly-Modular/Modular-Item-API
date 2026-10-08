package smartin.miapi.client.model.collision;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class Ray {

    private final Vector3f origin;
    private final Vector3f direction;

    public Ray(Vector3fc origin, Vector3fc direction) {
        this.origin = new Vector3f(origin);
        this.direction = new Vector3f(direction);

        if (this.direction.lengthSquared() < 1.0e-12f) {
            throw new IllegalArgumentException("Ray direction must not be zero");
        }

        this.direction.normalize();
    }

    public Vector3f origin() {
        return new Vector3f(origin);
    }

    public Vector3f direction() {
        return new Vector3f(direction);
    }

    public Vector3f at(float distance) {
        return at(distance, new Vector3f());
    }

    public Vector3f at(float distance, Vector3f destination) {
        return destination
                .set(direction)
                .mul(distance)
                .add(origin);
    }

    /**
     * Transform this ray by an affine matrix.
     *
     * The origin uses the full transformation.
     * The direction uses only the linear part.
     *
     * The transformed direction is normalized again. This means the returned
     * ray's distance parameter is in the transformed coordinate system.
     */
    public Ray transform(Matrix4f matrix) {
        Vector3f transformedOrigin =
                matrix.transformPosition(origin, new Vector3f());

        Vector3f transformedDirection =
                matrix.transformDirection(direction, new Vector3f())
                        .normalize();

        return new Ray(transformedOrigin, transformedDirection);
    }

    /**
     * Returns the signed distance from the ray origin to a point,
     * measured along the ray direction.
     */
    public float distanceTo(Vector3fc point) {
        return new Vector3f(point)
                .sub(origin)
                .dot(direction);
    }

    @Override
    public String toString() {
        return "Ray{" +
                "origin=" + origin +
                ", direction=" + direction +
                '}';
    }
}