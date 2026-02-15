package smartin.miapi.client.model.module.dynamic;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * small helper class to allow reversing inworld and local space using matrix stack and cam pos
 */
public class MatrixHelper {
    public static @NotNull Vector3f translatePositionToWorldSpace(
            PoseStack poseStack,
            Vector3f localPos,
            Vec3 camPos) {
        // Get current pose matrix
        Matrix4f pose = poseStack.last().pose();

        // Copy the local position to avoid mutating it
        Vector3f worldPos = new Vector3f(localPos);

        // Transform the vector by the pose matrix
        worldPos.mulPosition(pose); // transforms as a point (applies translation too)

        worldPos.add((float) camPos.x, (float) camPos.y, (float) camPos.z);

        return worldPos;
    }

    public static @NotNull Quaternionf translateQuaternionToWorldSpace(
            PoseStack poseStack,
            Quaternionf localRot
    ) {
        // Extract rotation from the pose matrix
        Quaternionf poseRotation =
                poseStack.last().pose().getNormalizedRotation(new Quaternionf());

        // world = poseRotation * local
        return new Quaternionf(poseRotation).mul(localRot);
    }

    public static @NotNull Quaternionf translateQuaternionToLocalSpace(
            PoseStack poseStack,
            Quaternionf worldRot
    ) {
        // Extract rotation from the pose matrix
        Quaternionf poseRotation =
                poseStack.last().pose().getNormalizedRotation(new Quaternionf());

        // Invert rotation
        Quaternionf inverse = poseRotation.invert(new Quaternionf());

        // local = inverse(poseRotation) * world
        return inverse.mul(worldRot);
    }



    public static Vector3f translatePositionToLocalSpace(
            PoseStack poseStack,
            Vector3f worldPos,
            Vec3 camPos) {
        Vector3f camRelative = new Vector3f(
                worldPos.x - (float) camPos.x,
                worldPos.y - (float) camPos.y,
                worldPos.z - (float) camPos.z
        );

        // Get current pose matrix
        Matrix4f pose = new Matrix4f(poseStack.last().pose());

        // Invert the matrix to go from world/camera space → local model space
        Matrix4f inverse = new Matrix4f(pose).invert();

        // Transform the camera-relative position into local space
        camRelative.mulPosition(inverse);

        return camRelative;
    }

    /**
     * Transforms a vector from model/local space to world space using the PoseStack.
     *
     * @param poseStack Current PoseStack
     * @param localVec  Vector in local/model space
     * @param camPose
     * @return Vector in world space
     */
    public static @NotNull Vector3f translateVectorToWorldSpace(
            PoseStack poseStack,
            Vector3f localVec,
            Vec3 camPose) {
        // Get current pose matrix (model → camera)
        Matrix4f model = new Matrix4f(poseStack.last().pose());

        // Transform the vector as a point (applies rotation + translation)
        Vector3f worldVec = new Vector3f(localVec);
        worldVec.mulPosition(model);

        // Add camera world position (PoseStack is camera-relative)
        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        worldVec.add((float) camPos.x, (float) camPos.y, (float) camPos.z);

        return worldVec;
    }

    /**
     * Transforms a vector from world space back to model/local space using the PoseStack.
     *
     * @param poseStack Current PoseStack
     * @param worldVec  Vector in world space
     * @param camPos
     * @return Vector in local/model space
     */
    public static @NotNull Vector3f translateVectorToLocalSpace(
            PoseStack poseStack,
            Vector3f worldVec,
            Vec3 camPos) {
        Vector3f camRelative = new Vector3f(
                worldVec.x - (float) camPos.x,
                worldVec.y - (float) camPos.y,
                worldVec.z - (float) camPos.z
        );

        // Get current pose matrix
        Matrix4f model = new Matrix4f(poseStack.last().pose());

        // Invert the matrix to go from camera/local space → local/model space
        Matrix4f inverse = new Matrix4f(model).invert();

        // Transform the camera-relative vector into local space
        camRelative.mulPosition(inverse);

        return camRelative;
    }

    public static Vec3 getCamPos() {
        return new Vec3(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f());
    }
}
