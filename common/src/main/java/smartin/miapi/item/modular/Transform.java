package smartin.miapi.item.modular;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.util.math.AffineTransformation;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.IOException;

/**
 * A Transform represents a transformation in 3D space, including rotation, translation, and scaling.
 * It extends the Transformation class with additional utility methods.
 */
@JsonAdapter(Transform.TransformJsonAdapter.class)
public class Transform {
    public String origin;
    public final Vector3f rotation;
    public final Vector3f translation;
    public final Vector3f scale;
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
        this.rotation = new Vector3f(rotation);
        this.translation = new Vector3f(translation);
        this.scale = new Vector3f(scale);
    }

    @Environment(EnvType.CLIENT)
    public Transform(Transformation transformation) {
        this.rotation = new Vector3f(transformation.rotation);
        this.translation = new Vector3f(transformation.translation);
        this.scale = new Vector3f(transformation.scale);
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

    @Environment(EnvType.CLIENT)
    public Transformation toTransformation() {
        return new Transformation(new Vector3f(rotation), new Vector3f(translation), new Vector3f(scale));
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
        childMatrix.mul(parentMatrix);
        return fromMatrix(childMatrix);
    }

    public Matrix4f toMatrix() {
        // Create the translation matrix
        Matrix4f translationMatrix = new Matrix4f().translate(translation);

        // Create the rotation matrix
        Matrix4f rotationMatrix = new Matrix4f()
                .rotateX((float) Math.toRadians(rotation.x))
                .rotateY((float) Math.toRadians(rotation.y))
                .rotateZ((float) Math.toRadians(rotation.z));

        // Create the scale matrix
        Matrix4f scaleMatrix = new Matrix4f().scale(scale);

        // Combine the matrices
        return new Matrix4f()
                .mul(translationMatrix)
                .mul(rotationMatrix)
                .mul(scaleMatrix);
    }

    public static Transform fromMatrix(Matrix4f matrix) {
        // Extract translation
        Vector3f translation = new Vector3f();
        matrix.getTranslation(translation);

        // Extract rotation (in Euler angles)
        Vector3f rotation = matrix.getEulerAnglesXYZ(new Vector3f());
        rotation.x = (float) Math.toDegrees(rotation.x());
        rotation.y = (float) Math.toDegrees(rotation.y());
        rotation.z = (float) Math.toDegrees(rotation.z());

        // Extract scale
        Vector3f scale = matrix.getScale(new Vector3f());

        return new Transform(rotation, translation, scale);
    }


    /**
     * Creates a new copy of this Transform.
     *
     * @return the new Transform copy
     */
    public Transform copy() {
        Transform copy = new Transform(
                this.rotation != null ? new Vector3f(this.rotation) : new Vector3f(0, 0, 0),
                this.translation != null ? new Vector3f(this.translation) : new Vector3f(0, 0, 0),
                this.scale != null ? new Vector3f(this.scale) : new Vector3f(1, 1, 1)
        );

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
        return new Transform(new Vector3f(parentRotation), new Vector3f(parentTranslation), new Vector3f(parentScale)).withOrigin(transformation.origin);
    }

    public Transform withOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    /**
     * Converts a Transformation into a model Transformation by scaling the translation vector by 1/16.
     *
     * @param transformation the Transformation to convert, as a Transformation
     * @return the new model Transformation, as a Transform
     */
    public static Transform toModelTransformation(Transform transformation) {
        Transform transform = repair(transformation);
        transform.translation.mul(1.0f / 16.0f);
        return transform;
    }

    /**
     * Creates an AffineTransformation from this Transform.
     *
     * @return an AffineTransformation with the rotation, translation, and scale from this Transform.
     */
    @Environment(EnvType.CLIENT)
    public AffineTransformation toAffineTransformation() {
        Transform transform = this.copy();
        Quaternionf quaternionf = new Quaternionf();
        quaternionf.rotationXYZ(
                (float) Math.toRadians(this.rotation.x),
                (float) Math.toRadians(this.rotation.y),
                (float) Math.toRadians(this.rotation.z)
        );
        Vector3f translationVector = new Vector3f(transform.translation);
        Vector3f scaleVector = new Vector3f(transform.scale);
        return new AffineTransformation(translationVector, quaternionf, scaleVector, quaternionf);
    }

    public int[] rotateVertexData(int[] vertexData) {
        for (int i = 0; i < vertexData.length; i += 8) {
            // Extract position components from vertex data
            float x = Float.intBitsToFloat(vertexData[i]);
            float y = Float.intBitsToFloat(vertexData[i + 1]);
            float z = Float.intBitsToFloat(vertexData[i + 2]);

            // Create Vector4f representing the position (X, Y, Z, 1.0)
            Vector4f position = new Vector4f(x, y, z, 1.0f);

            // Apply the transformation to the position
            Vector4f transformedPosition = this.toMatrix().transform(position);


            // Extract the transformed position components
            float transformedX = transformedPosition.x;
            float transformedY = transformedPosition.y;
            float transformedZ = transformedPosition.z;

            // Update the vertex array with the new transformed position values
            vertexData[i] = Float.floatToIntBits(transformedX);
            vertexData[i + 1] = Float.floatToIntBits(transformedY);
            vertexData[i + 2] = Float.floatToIntBits(transformedZ);
        }
        return vertexData;
    }

    /**
     * Creates an ModelBakeSettings from this Transform
     *
     * @return a ModelBakeSettings
     */
    @Environment(EnvType.CLIENT)
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

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null) {
            return false;
        } else if (o.getClass() != this.getClass()) {
            return false;
        } else {
            Transformation transformation = (Transformation) o;
            return this.rotation.equals(transformation.rotation) && this.scale.equals(transformation.scale) && this.translation.equals(transformation.translation);
        }
    }

    public int hashCode() {
        int i = this.rotation.hashCode();
        i = 31 * i + this.translation.hashCode();
        i = 31 * i + this.scale.hashCode();
        return i;
    }

    public static class TransformJsonAdapter extends TypeAdapter<Transform> {
        @Override
        public void write(JsonWriter jsonWriter, Transform transform) throws IOException {
            jsonWriter.beginObject();
            jsonWriter.name("origin").value(transform.origin);
            writeVector3f(jsonWriter, "rotation", transform.rotation);
            writeVector3f(jsonWriter, "translation", transform.translation);
            writeVector3f(jsonWriter, "scale", transform.scale);
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

        private void writeVector3f(JsonWriter jsonWriter, String name, Vector3f vector3f) throws IOException {
            jsonWriter.name(name);
            jsonWriter.beginArray();
            jsonWriter.value(vector3f.x);
            jsonWriter.value(vector3f.y);
            jsonWriter.value(vector3f.z);
            jsonWriter.endArray();
        }

        private Vector3f readVector3f(JsonReader jsonReader) throws IOException {
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
