package smartin.miapi.client.model.module.dynamic;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * small helper class to allow reversing inworld and local space using matrix stack and cam pos
 */
public class MatrixHelper {
    public static @NotNull Vector3f translatePositionToWorldSpace(
            PoseStack poseStack,
            Vector3f localPos,
            Vec3 camPos) {
        Matrix4f pose = poseStack.last().pose();

        return translatePositionToWorldSpace(localPos, camPos, pose);
    }

    public static @NotNull Vector3f translatePositionToWorldSpace(Vector3f localPos, Vec3 camPos, Matrix4f pose) {
        Vector3f worldPos = new Vector3f(localPos);
        worldPos.mulPosition(pose);
        worldPos.add((float) camPos.x, (float) camPos.y, (float) camPos.z);
        return worldPos;
    }


    public static Vector3f translatePositionToLocalSpace(
            PoseStack poseStack,
            Vector3f worldPos,
            Vec3 camPos) {
        Matrix4f pose = new Matrix4f(poseStack.last().pose());

        return translatePositionToLocalSpace(worldPos, camPos, pose);
    }

    public static @NotNull Vector3f translatePositionToLocalSpace(Vector3f worldPos, Vec3 camPos, Matrix4f pose) {
        Vector3f camRelative = new Vector3f(
                worldPos.x - (float) camPos.x,
                worldPos.y - (float) camPos.y,
                worldPos.z - (float) camPos.z
        );

        Matrix4f inverse = new Matrix4f(pose).invert();
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
        Matrix4f model = new Matrix4f(poseStack.last().pose());

        return translateVectorToWorldSpace(localVec, model);
    }

    public static @NotNull Vector3f translateVectorToWorldSpace(Vector3f localVec, Matrix4f model) {
        Vector3f worldVec = new Vector3f(localVec);
        worldVec.mulPosition(model);
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
        Matrix4f model = new Matrix4f(poseStack.last().pose());

        return translateVectorToLocalSpace(worldVec, camPos, model);
    }

    public static @NotNull Vector3f translateVectorToLocalSpace(Vector3f worldVec, Vec3 camPos, Matrix4f model) {
        Vector3f camRelative = new Vector3f(
                worldVec.x - (float) camPos.x,
                worldVec.y - (float) camPos.y,
                worldVec.z - (float) camPos.z
        );
        Matrix4f inverse = new Matrix4f(model).invert();
        camRelative.mulPosition(inverse);

        return camRelative;
    }

    public static Vec3 getCamPos() {
        return new Vec3(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f());
    }
}
