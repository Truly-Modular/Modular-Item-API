package smartin.miapi.item.modular;

import com.google.gson.Gson;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.util.math.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * A Transform represents a transformation in 3D space, including rotation, translation, and scaling.
 * It extends the Transformation class with additional utility methods.
 */
public class Transform extends Transformation {
    public String origin;
    /**
     * The identity bakedTransform, representing no transformation at all.
     */
    public static final Transform IDENTITY = new Transform(new Vector3f(), new Vector3f(), new Vector3f(1.0F, 1.0F, 1.0F));

    /**
     * Creates a new Transform with the given rotation, translation, and scale.
     *
     * @param rotation    the rotation vector, as a Vec3f
     * @param translation the translation vector, as a Vec3f
     * @param scale       the scale vector, as a Vec3f
     */
    public Transform(Vector3f rotation, Vector3f translation, Vector3f scale) {
        super(rotation.copy(), translation.copy(), scale.copy());
    }

    public Transform(Transformation transformation) {
        super(transformation.rotation.copy(), transformation.translation.copy(), transformation.scale.copy());
    }

    /**
     * Merges two Transformations into a new Transform. This Transform is applied first, followed by the child.
     *
     * @param child the child transformation, as a Transformation
     * @return the merged transformation, as a new Transform
     */
    public Transform merge(Transform child) {
        return Transform.merge(this, child);
    }

    /**
     * Merges two Transformations into a new Transform. The parent transformation is applied first, followed by the child.
     *
     * @param parent        the parent transformation, as a Transformation
     * @param originalChild the child transformation, as a Transformation
     * @return the merged transformation, as a new Transform
     */
    public static Transform merge(Transform parent, Transform originalChild) {
        Transform child = originalChild.copy();
        parent = parent.copy();
        Matrix4f parentMatrix = parent.toMatrix();

        Matrix4f childMatrix = child.toMatrix();
        childMatrix.multiply(parentMatrix);
        return fromMatrix(childMatrix);
    }

    /**
     * Applies the transformation to a vector in 3D space.
     *
     * @param vector the vector to bakedTransform, as a Vec3f
     * @return the transformed vector, as a new Vec3f
     */
    public Vector3f transformVector(Vector3f vector) {
        // Apply scaling
        float x = vector.getX() * scale.getX();
        float y = vector.getY() * scale.getY();
        float z = vector.getZ() * scale.getZ();

        // Apply rotation
        float cosX = MathHelper.cos(rotation.getX());
        float sinX = MathHelper.sin(rotation.getX());
        float cosY = MathHelper.cos(rotation.getY());
        float sinY = MathHelper.sin(rotation.getY());
        float cosZ = MathHelper.cos(rotation.getZ());
        float sinZ = MathHelper.sin(rotation.getZ());

        float x2 = cosY * (sinZ * y + cosZ * x) - sinY * z;
        float y2 = sinX * (cosY * z + sinY * (sinZ * y + cosZ * x)) + cosX * (cosZ * y - sinZ * x);
        float z2 = cosX * (cosY * z + sinY * (sinZ * y + cosZ * x)) - sinX * (cosZ * y - sinZ * x);

        x = x2;
        y = y2;
        z = z2;

        // Apply translation
        x += translation.getX();
        y += translation.getY();
        z += translation.getZ();

        return new Vec3f(x, y, z);
    }

    public Matrix4f toMatrix() {
        Matrix4f matrix4f = Matrix4f.translate(translation.getX(), translation.getY(), translation.getZ());
        matrix4f.multiply(new Matrix4f(Quaternion.fromEulerXyzDegrees(rotation.copy())));
        matrix4f.multiply(Matrix4f.scale(scale.getX(), scale.getY(), scale.getZ()));
        return matrix4f;
    }

    /**
     * Creates a new copy of this Transform.
     *
     * @return the new Transform copy
     */
    public Transform copy() {
        Transform copy = new Transform(this.rotation.copy(), this.translation.copy(), this.scale.copy());
        copy.origin = this.origin;
        return copy;
    }

    /**
     * Repairs a Transformation by replacing null rotation, translation, or scale vectors with default values.
     *
     * @param transformation the Transformation to repair, as a Transformation
     * @return the repaired transformation, as a new Transform
     */
    public static Transform repair(Transformation transformation) {
        Vector3f parentRotation = transformation.rotation;
        if (parentRotation == null) {
            parentRotation = new Vector3f(0, 0, 0);
        }
        Vector3f parentTranslation = transformation.translation;
        if (parentTranslation == null) {
            parentTranslation = new Vector3f(0, 0, 0);
        }
        Vector3f parentScale = transformation.scale;
        if (parentScale == null) {
            parentScale = new Vector3f(1, 1, 1);
        }
        return new Transform(new Vector3f(parentRotation), new Vector3f(parentTranslation), new Vector3f(parentScale));
    }

    /**
     * Converts a Transformation into a model Transformation by scaling the translation vector by 1/16.
     *
     * @param transformation the Transformation to convert, as a Transformation
     * @return the new model Transformation, as a Transform
     */
    public static Transform toModelTransformation(Transformation transformation) {
        Transform transform = repair(transformation);
        //TODO:enable this and change all jsons
        transform.translation.scale(1.0f / 16.0f);
        //transform.translation.multiplyComponentwise(transform.scale.getX(), transform.scale.getY(), transform.scale.getZ());
        return transform;
    }

    /**
     * Creates an AffineTransformation from this Transform.
     *
     * @return an AffineTransformation with the rotation, translation, and scale from this Transform.
     */
    public AffineTransformation toAffineTransformation() {
        Transform transform = this.copy();
        Quaternion rotation = Quaternion.fromEulerXyzDegrees(transform.rotation.copy());
        Vec3f translationVector = transform.translation.copy();
        //translationVector.multiplyComponentwise(1 / scale.getX(), 1 / scale.getY(), 1 / scale.getZ());
        AffineTransformation affineTransformation = new AffineTransformation(translationVector, null, transform.scale.copy(), rotation);
        //affineTransformation = new AffineTransformation(this.toMatrix());
        return affineTransformation;
    }

    /**
     * Creates an ModelBakeSettings from this Transform
     *
     * @return a ModelBakeSettings
     */
    public ModelBakeSettings toModelBakeSettings() {
        Transform transform = toModelTransformation(this);
        AffineTransformation affineTransformation = transform.toAffineTransformation();
        return new ModelBakeSettings() {
            @Override
            public AffineTransformation getRotation() {
                return affineTransformation;
            }

            @Override
            public boolean isUvLocked() {
                return false;
            }
        };
    }

    public static Transform fromMatrix(Matrix4f matrix4f) {
        AffineTransformation affineTransformation = new AffineTransformation(matrix4f);
        Vec3f translation = affineTransformation.getTranslation();
        return new Transform(affineTransformation.getRotation2().toEulerXyzDegrees(), translation, affineTransformation.getScale());
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
