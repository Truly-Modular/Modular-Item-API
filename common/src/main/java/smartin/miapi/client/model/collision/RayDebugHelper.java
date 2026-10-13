package smartin.miapi.client.model.collision;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Vector3f;

public final class RayDebugHelper {

    private static final float EPSILON = 1.0E-6F;
    private static final float ARROW_SIZE = 0.08F;
    private static final float TARGET_ARROW_SIZE = 0.25F;

    private static final float MIN_TARGET_ARROW_LENGTH = 8.00F;
    private static final float MAX_TARGET_ARROW_LENGTH = 1000.00F;

    private RayDebugHelper() {
    }

    public static void renderDebugRay(
            Ray ray,
            PoseStack matrices,
            MultiBufferSource buffers,
            float length
    ) {
        if (ray == null || buffers == null || length <= 0.0F) {
            return;
        }

        PoseStack.Pose pose = matrices.last();

        // The ray is already in local space.
        Vector3f start = new Vector3f(ray.origin());
        Vector3f end = new Vector3f(ray.at(length));

        float rayLength = new Vector3f(end).sub(start).length();

        if (rayLength < EPSILON) {
            return;
        }

        VertexConsumer lines = buffers.getBuffer(RenderType.lines());

        // Original ray: red.
        drawArrow(
                lines, pose, start, end,
                255, 0, 0,
                ARROW_SIZE
        );

        // Set the auxiliary arrow length.
        float targetArrowLength = Math.min(
                Math.max(rayLength * 0.25F, MIN_TARGET_ARROW_LENGTH),
                MAX_TARGET_ARROW_LENGTH
        );

        // Blue arrow: from above, pointing down at the target.
        Vector3f topStart = new Vector3f(end)
                .add(0.0F, targetArrowLength, 0.0F);

        drawArrow(
                lines, pose, topStart, end,
                0, 100, 255,
                TARGET_ARROW_SIZE
        );

        // Green arrow: from the right, pointing left at the target.
        Vector3f rightStart = new Vector3f(end)
                .add(targetArrowLength, 0.0F, 0.0F);

        drawArrow(
                lines, pose, rightStart, end,
                0, 255, 0,
                TARGET_ARROW_SIZE
        );
    }

    private static void drawArrow(
            VertexConsumer lines,
            PoseStack.Pose pose,
            Vector3f start,
            Vector3f end,
            int red,
            int green,
            int blue,
            float headSize
    ) {
        Vector3f direction = new Vector3f(end).sub(start);
        float arrowLength = direction.length();

        if (arrowLength < EPSILON) {
            return;
        }

        direction.div(arrowLength);

        // Draw the arrow shaft.
        drawDebugLine(
                lines, pose, start, end,
                red, green, blue
        );

        // Calculate the arrowhead.
        float headLength = arrowLength * headSize;

        Vector3f side = new Vector3f(direction)
                .cross(0.0F, 1.0F, 0.0F);

        if (side.lengthSquared() < EPSILON) {
            side.set(direction)
                    .cross(1.0F, 0.0F, 0.0F);
        }

        side.normalize().mul(headLength * 0.5F);

        Vector3f base = new Vector3f(end)
                .fma(-headLength, direction);

        drawDebugLine(
                lines, pose, end,
                new Vector3f(base).add(side),
                red, green, blue
        );

        drawDebugLine(
                lines, pose, end,
                new Vector3f(base).sub(side),
                red, green, blue
        );
    }

    private static void drawDebugLine(
            VertexConsumer lines,
            PoseStack.Pose pose,
            Vector3f start,
            Vector3f end,
            int red,
            int green,
            int blue
    ) {
        Vector3f direction = new Vector3f(end).sub(start);

        if (direction.lengthSquared() < EPSILON) {
            return;
        }

        direction.normalize();

        Vector3f normal = new Vector3f(direction)
                .cross(0.0F, 1.0F, 0.0F);

        if (normal.lengthSquared() < EPSILON) {
            normal.set(direction)
                    .cross(1.0F, 0.0F, 0.0F);
        }

        normal.normalize();

        lines.addVertex(
                        pose.pose(),
                        start.x, start.y, start.z
                )
                .setColor(red, green, blue, 255)
                .setNormal(pose, normal.x, normal.y, normal.z);

        lines.addVertex(
                        pose.pose(),
                        end.x, end.y, end.z
                )
                .setColor(red, green, blue, 255)
                .setNormal(pose, normal.x, normal.y, normal.z);
    }
}