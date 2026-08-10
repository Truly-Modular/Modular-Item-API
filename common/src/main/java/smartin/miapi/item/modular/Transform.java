package smartin.miapi.item.modular;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.world.entity.Entity;
import org.joml.*;
import smartin.miapi.Miapi;

import java.io.IOException;
import java.lang.Math;
import java.util.List;
import java.util.Optional;

/**
 * A Transform represents a transformation in 3D space, including rotation, translation, and scaling.
 * It stores a Matrix4f internally and provides utility methods to extract components.
 */
@JsonAdapter(Transform.TransformJsonAdapter.class)
public class Transform {
    @CodecBehavior.Optional
    public String origin;
    private Matrix4f matrix;
    private Vector3f translation;
    private Vector3f rotation;
    private Vector3f scale;

    public static final Codec<Vector3f> VECTOR_CODEC = Codec.withAlternative(AutoCodec.of(Vector3f.class).codec(),
            Codec.list(Codec.DOUBLE).xmap((list) ->
                            new Vector3f(
                                    list.getFirst().floatValue(),
                                    list.get(1).floatValue(),
                                    list.get(2).floatValue()),
                    vector -> List.of((double) vector.x(), (double) vector.y(), (double) vector.z())));

    public static final Codec<Transform> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    VECTOR_CODEC.optionalFieldOf("rotation", new Vector3f(0, 0, 0))
                            .forGetter(Transform::getRotation),
                    VECTOR_CODEC.optionalFieldOf("translation", new Vector3f(0, 0, 0))
                            .forGetter(Transform::getTranslation),
                    VECTOR_CODEC.optionalFieldOf("scale", new Vector3f(1, 1, 1))
                            .forGetter(Transform::getScale),
                    Codec.STRING.optionalFieldOf("origin")
                            .forGetter((transform) -> Optional.ofNullable(transform.origin))
            ).apply(instance, Transform::new)
    );

    /**
     * The identity bakedTransform, representing no transformation at all.
     */
    public static final Transform IDENTITY = new Transform(new Matrix4f().identity());


    /**
     * Creates a new Transform with the given Matrix4f.
     *
     * @param matrix the matrix to use
     */
    public Transform(Matrix4f matrix) {
        this.matrix = new Matrix4f(matrix);
        this.origin = null;

        validateMatrix(this.matrix);
    }

    private static void validateMatrix(Matrix4f matrix) {
        if (!matrix.isFinite()) {
            Miapi.LOGGER.error("Invalid transform matrix contains NaN or Infinity: {}", matrix);
            return;
        }

        float determinant = matrix.determinant();
        if (Math.abs(determinant) < 1.0E-6f) {
            Miapi.LOGGER.error("Invalid transform matrix has zero determinant: {}", matrix);
            return;
        }


        if (!matrix.isAffine()) {
            Miapi.LOGGER.warn(
                    "TransformMatrix received non-affine matrix. " +
                    "This looks like a projection matrix or contains perspective data: {}",
                    matrix
            );
        }

        if (!isOrthonormal(matrix)) {
            Miapi.LOGGER.warn(
                    "TransformMatrix rotation basis is not orthonormal. " +
                    "Matrix may contain shear or invalid scaling: {}",
                    matrix
            );
        }
    }

    private static boolean isOrthonormal(Matrix4f matrix) {
        Vector3f x = matrix.getColumn(0, new Vector3f());
        Vector3f y = matrix.getColumn(1, new Vector3f());
        Vector3f z = matrix.getColumn(2, new Vector3f());

        float xy = Math.abs(x.dot(y));
        float xz = Math.abs(x.dot(z));
        float yz = Math.abs(y.dot(z));

        return xy < 1E-4f && xz < 1E-4f && yz < 1E-4f;
    }

    /**
     * Creates a new Transform with the given rotation, translation, and scale.
     *
     * @param rotation    the rotation vector, as a Vec3f
     * @param translation the translation vector, as a Vec3f
     * @param scale       the scale vector, as a Vec3f
     */
    public Transform(Vector3f rotation, Vector3f translation, Vector3f scale) {
        this(rotation, translation, scale, Optional.empty());
    }

    /**
     * Creates a new Transform with the given rotation, translation, and scale.
     *
     * @param rotation    the rotation vector, as a Vec3f
     * @param translation the translation vector, as a Vec3f
     * @param scale       the scale vector, as a Vec3f
     * @param origin      the origin string
     */
    public Transform(Vector3f rotation, Vector3f translation, Vector3f scale, Optional<String> origin) {
        this(toMatrix(rotation, translation, scale));
        origin.ifPresent(string -> this.origin = string);
        this.rotation = rotation;
        this.translation = translation;
        this.scale = scale;
    }


    @Environment(EnvType.CLIENT)
    public Transform(ItemTransform transformation) {
        this(toMatrix(transformation.rotation, transformation.translation, transformation.scale));
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
     * @param parent the parent transformation, as a Transformation
     * @param child  the child transformation, as a Transformation
     * @return the merged transformation, as a new Transform
     */
    public static Transform merge(Transform parent, Transform child) {
        Matrix4f merged = new Matrix4f(parent.matrix)
                .mul(child.matrix);

        return new Transform(merged);
    }

    @Environment(EnvType.CLIENT)
    public ItemTransform toTransformation() {
        return new ItemTransform(
                new Vector3f(getRotation()),
                new Vector3f(getTranslation().div(16)),
                new Vector3f(getScale())
        );
    }

    /**
     * Extracts the translation vector from this transformation.
     *
     * @return the translation vector
     */
    public Vector3f getTranslation() {
        return new Vector3f(matrix.getTranslation(new Vector3f()));
    }

    /**
     * Extracts the rotation vector (in degrees) from this transformation.
     *
     * @return the rotation vector in degrees
     */
    public Vector3f getRotation() {
        Matrix4f rotationMatrix = new Matrix4f(matrix);
        // Remove translation
        rotationMatrix.m30(0);
        rotationMatrix.m31(0);
        rotationMatrix.m32(0);

        // Remove scale
        Vector3f scale = new Vector3f();
        rotationMatrix.getScale(scale);

        rotationMatrix.scale(
                1.0f / scale.x,
                1.0f / scale.y,
                1.0f / scale.z
        );
        return getEulerAnglesXYZ(rotationMatrix,new Vector3f()).mul(57.29577951308232f);
    }

    public Vector3f getEulerAnglesXYZ(Matrix4f rotationMatrix, Vector3f dest) {
        float sy = rotationMatrix.m20();

        // epsilon instead of exact 1 due to floating point error
        if (Math.abs(sy) < 0.999999f) {
            // Normal case
            dest.x = (float) Math.atan2(-rotationMatrix.m21(), rotationMatrix.m22());
            dest.y = (float) Math.atan2(sy, Math.sqrt(1.0f - sy * sy));
            dest.z = (float) Math.atan2(-rotationMatrix.m10(), rotationMatrix.m00());
        } else {
            // Gimbal lock
            dest.y = sy > 0
                    ? (float) (Math.PI * 0.5)
                    : (float) (-Math.PI * 0.5);

            // Arbitrarily choose Z = 0
            dest.z = 0.0f;

            if (sy > 0) {
                // +90°
                dest.x = (float) Math.atan2(rotationMatrix.m01(), rotationMatrix.m11());
            } else {
                // -90°
                dest.x = (float) Math.atan2(-rotationMatrix.m01(), rotationMatrix.m11());
            }
        }

        return dest;
    }

    private static final float EPS = 0.1f;

    private static Vector3f fixEuler(Vector3f euler) {
        //euler.x = wrapAndDeGimbal(euler.x);
        //euler.y = wrapAndDeGimbal(euler.y);
        //euler.z = wrapAndDeGimbal(euler.z);

        return euler;
    }

    private static float wrapAndDeGimbal(float angle) {
        angle %= 360f;
        if (angle <= -180f) angle += 360f;
        if (angle > 180f) angle -= 360f;
        if (Math.abs(Math.abs(angle) - 90f) < EPS) {
            angle += EPS;
        }
        return angle;
    }

    /**
     * Extracts the scale vector from this transformation.
     *
     * @return the scale vector
     */
    public Vector3f getScale() {
        return matrix.getScale(new Vector3f());
    }

    public static void applyPosition(PoseStack matrixStack, Matrix4f matrix4f) {
        matrixStack.mulPose(matrix4f);
    }

    public static void applyPosition(PoseStack matrixStack, Transform transform) {
        applyPosition(matrixStack, transform.matrix);
    }

    public void applyPosition(PoseStack matrixStack) {
        applyPosition(matrixStack, this.matrix);
    }

    public Matrix4f toMatrix() {
        return matrix;
    }

    /**
     * Creates a new copy of this Transform.
     *
     * @return the new Transform copy
     */
    public Transform copy() {
        Transform copy = new Transform(new Matrix4f(matrix));
        copy.origin = this.origin;
        return copy;
    }

    /**
     * Repairs a Transformation by replacing null rotation, translation, or scale vectors with default values.
     *
     * @param transformation the Transformation to repair, as a Transformation
     * @return the repaired transformation, as a new Transform
     */
    public static Transform repair(Transform transformation) {
        Matrix4f matrix = new Matrix4f();
        if (transformation.matrix != null) {
            matrix.set(transformation.matrix);
        }
        return new Transform(matrix).withOrigin(transformation.origin);
    }

    public Transform withOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    public static Matrix4f toMatrix(Vector3f rotation, Vector3f translation, Vector3f scale) {
        rotation = fixEuler(rotation);
        Matrix4f translationMatrix = new Matrix4f().translate(translation);
        Matrix4f scaleMatrix = new Matrix4f().scale(scale);
        return new Matrix4f()
                .mul(translationMatrix)
                .rotate((new Quaternionf()).rotationXYZ(rotation.x() * 0.017453292F, rotation.y() * 0.017453292F, rotation.z() * 0.017453292F))
                .mul(scaleMatrix);
    }

    /**
     * Converts a Transformation into a model Transformation by scaling the translation vector by 1/16.
     *
     * @param transformation the Transformation to convert, as a Transformation
     * @return the new model Transformation, as a Transform
     */
    public static Transform toModelTransformation(Transform transformation) {
        Transform transform = repair(transformation);

        Matrix4f matrix = new Matrix4f(transform.matrix);
        Vector3f translation = matrix.getTranslation(new Vector3f());
        translation.mul(1.0f / 16.0f);
        matrix.setTranslation(translation);

        return new Transform(matrix);
    }

    public int[] rotateVertexData(int[] vertexData) {
        int[] rotatedData = vertexData.clone();

        Matrix4f transform = this.matrix;
        Matrix3f normalMatrix = new Matrix3f(transform);

        for (int i = 0; i < rotatedData.length; i += 8) {
            // Rotate position
            float x = Float.intBitsToFloat(rotatedData[i]);
            float y = Float.intBitsToFloat(rotatedData[i + 1]);
            float z = Float.intBitsToFloat(rotatedData[i + 2]);

            Vector4f position = new Vector4f(x, y, z, 1.0f);
            transform.transform(position);

            rotatedData[i] = Float.floatToRawIntBits(position.x);
            rotatedData[i + 1] = Float.floatToRawIntBits(position.y);
            rotatedData[i + 2] = Float.floatToRawIntBits(position.z);

            // Rotate packed normal (if present)
            int packedNormal = rotatedData[i + 7];
            if (packedNormal != 0) {
                byte nx = (byte) (packedNormal & 0xFF);
                byte ny = (byte) ((packedNormal >> 8) & 0xFF);
                byte nz = (byte) ((packedNormal >> 16) & 0xFF);

                Vector3f normal = new Vector3f(
                        nx / 127.0f,
                        ny / 127.0f,
                        nz / 127.0f
                );

                normalMatrix.transform(normal).normalize();

                int rx = Math.max(-127, Math.min(127, Math.round(normal.x * 127.0f)));
                int ry = Math.max(-127, Math.min(127, Math.round(normal.y * 127.0f)));
                int rz = Math.max(-127, Math.min(127, Math.round(normal.z * 127.0f)));

                rotatedData[i + 7] =
                        (rx & 0xFF) |
                        ((ry & 0xFF) << 8) |
                        ((rz & 0xFF) << 16);
            }
        }

        return rotatedData;
    }

    /**
     * Helper to convert an entities position+rotation into a Transform Object for easier work
     *
     * @param entity
     * @return
     */
    public static Transform getTransform(Entity entity) {
        return new Transform(
                new Vector3f(entity.getXRot(), entity.getYRot(), 0f),
                new Vector3f((float) entity.getX(), (float) entity.getY(), (float) entity.getZ()),
                new Vector3f(1, 1, 1)
        );
    }

    /**
     * Helper method to easily set an entities position via a Transform
     *
     * @param entity
     * @param transform
     */
    public static void setTransform(Entity entity, Transform transform) {
        entity.setPos(
                transform.getTranslation().x,
                transform.getTranslation().y,
                transform.getTranslation().z
        );

        //entity.setXRot(transform.getRotation().x);
        //entity.setYRot(transform.getRotation().y);

        //entity.setXRot(entity.getXRot());
        //entity.setYRot(entity.getYRot());
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null) {
            return false;
        } else if (o.getClass() != this.getClass()) {
            return false;
        } else {
            Transform transformation = (Transform) o;
            return this.getRotation().equals(transformation.getRotation()) &&
                   this.getScale().equals(transformation.getScale()) &&
                   this.getTranslation().equals(transformation.getTranslation());
        }
    }

    @Override
    public int hashCode() {
        int i = this.getRotation().hashCode();
        i = 31 * i + this.getTranslation().hashCode();
        i = 31 * i + this.getScale().hashCode();
        return i;
    }

    public static class TransformJsonAdapter extends TypeAdapter<Transform> {
        @Override
        public void write(JsonWriter jsonWriter, Transform transform) throws IOException {
            jsonWriter.beginObject();
            jsonWriter.name("origin").value(transform.origin);
            writeVector3f(jsonWriter, "rotation", transform.getRotation());
            writeVector3f(jsonWriter, "translation", transform.getTranslation());
            writeVector3f(jsonWriter, "scale", transform.getScale());
            jsonWriter.endObject();
        }

        @Override
        public Transform read(JsonReader jsonReader) throws IOException {
            Vector3f rotation = null;
            Vector3f translation = null;
            Vector3f scale = null;
            String origin = null;

            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                if ("origin".equals(name)) {
                    origin = jsonReader.nextString();
                } else if ("rotation".equals(name)) {
                    rotation = readVector3f(jsonReader);
                } else if ("translation".equals(name)) {
                    translation = readVector3f(jsonReader);
                } else if ("scale".equals(name)) {
                    scale = readVector3f(jsonReader);
                } else {
                    jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
            // Ensure non-null values for final fields
            if (rotation == null) {
                rotation = new Vector3f();
            }
            if (translation == null) {
                translation = new Vector3f();
            }
            if (scale == null) {
                scale = new Vector3f();
            }
            Transform transform = new Transform(rotation, translation, scale);
            transform.origin = origin;
            return transform;
        }

        private static void writeVector3f(JsonWriter jsonWriter, String name, Vector3f vector3f) throws IOException {
            jsonWriter.name(name);
            jsonWriter.beginArray();
            jsonWriter.value(vector3f.x);
            jsonWriter.value(vector3f.y);
            jsonWriter.value(vector3f.z);
            jsonWriter.endArray();
        }

        private static Vector3f readVector3f(JsonReader jsonReader) throws IOException {
            Vector3f vector3f = new Vector3f();

            if (jsonReader.peek() == JsonToken.BEGIN_ARRAY) {
                // Read as an array
                jsonReader.beginArray();
                vector3f.x = (float) jsonReader.nextDouble();
                vector3f.y = (float) jsonReader.nextDouble();
                vector3f.z = (float) jsonReader.nextDouble();
                jsonReader.endArray();
            } else if (jsonReader.peek() == JsonToken.BEGIN_OBJECT) {
                // Read as an object with components
                jsonReader.beginObject();
                while (jsonReader.hasNext()) {
                    String propName = jsonReader.nextName();
                    if ("x".equals(propName)) {
                        vector3f.x = (float) jsonReader.nextDouble();
                    } else if ("y".equals(propName)) {
                        vector3f.y = (float) jsonReader.nextDouble();
                    } else if ("z".equals(propName)) {
                        vector3f.z = (float) jsonReader.nextDouble();
                    }
                }
                jsonReader.endObject();
            }

            return vector3f;
        }
    }
}
