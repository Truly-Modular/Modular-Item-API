package smartin.miapi.client.model.module.dynamic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import smartin.miapi.client.model.module.BakedMiapiModel;
import smartin.miapi.client.model.module.dynamic.verlet.ChainNode;
import smartin.miapi.client.model.module.dynamic.verlet.ChainSegment;
import smartin.miapi.client.model.module.dynamic.verlet.ChainSimulationState;
import smartin.miapi.client.model.module.dynamic.verlet.VerletIntegrator;
import smartin.miapi.item.modular.Transform;

import java.util.List;

public class ChainModel extends DynamicModel<ChainModel.ChainState> {

    private final List<ChainEntry> chain;

    private final float radius;
    public final Transform transform;
    public final boolean debug;

    public ChainModel(
            List<ChainEntry> chain,
            float radius,
            Transform transform,
            boolean debug
    ) {
        this.chain = chain;
        this.radius = radius;
        this.transform = transform;
        this.debug = debug;

        float len = 0f;
        for (ChainEntry e : chain)
            len += e.length;
        float totalLength = len;
    }

    @Override
    protected ChainState createState(Vec3 pos, RenderContext context) {
        return new ChainState(chain, context, pos);
    }


    public static class ChainState extends SimulationState {

        public final ChainSimulationState sim;

        public ChainState(List<ChainEntry> chain, RenderContext context, Vec3 cameraPos) {
            sim = new ChainSimulationState();

            int nodeCount = chain.size() + 1;
            sim.nodes = new ChainNode[nodeCount];

            // World-space anchor
            Vector3f localOrigin = new Vector3f(0, 0, 0);
            Vector3f worldOrigin = MatrixHelper.translateVectorToWorldSpace(context.matrices(), localOrigin, cameraPos);

            for (int i = 0; i < nodeCount; i++) {
                sim.nodes[i] = new ChainNode();
                sim.nodes[i].pos.set(worldOrigin);
            }

            // Anchor node
            sim.nodes[0].locked = true;

            Vector3f cursor = new Vector3f(worldOrigin);

            for (int i = 0; i < chain.size(); i++) {
                ChainEntry e = chain.get(i);
                ChainNode next = sim.nodes[i + 1];

                next.locked = e.locked;
                next.collide = e.collide;
                next.gravity = e.gravity;

                float dir = (i % 2 == 0) ? 1f : -1f;

                // ---- WORLD SPACE LENGTH DERIVATION ----
                Vector3f localOffset = new Vector3f(0, e.length * dir, 0);
                Vector3f worldOffset =
                        MatrixHelper.translateVectorToWorldSpace(context.matrices(), localOffset, cameraPos)
                                .sub(worldOrigin);

                cursor.add(worldOffset);
                next.pos.set(cursor);
            }

            // Create segments with WORLD lengths
            sim.segments = new ChainSegment[chain.size()];
            for (int i = 0; i < chain.size(); i++) {
                ChainEntry e = chain.get(i);

                Vector3f localOffset = new Vector3f(0, e.length, 0);
                float worldLength =
                        MatrixHelper.translateVectorToWorldSpace(context.matrices(), localOffset, cameraPos)
                                .sub(worldOrigin)
                                .length();

                sim.segments[i] = new ChainSegment(i, i + 1, worldLength);
                sim.segments[i].collide = e.collide;
            }
        }


    }

    @Override
    public void updatePhysics(
            ChainState state,
            LivingEntity entity,
            float delta,
            PoseStack pose,
            Vector3f down,
            ItemDisplayContext c
    ) {
        Vector3f currentPos = MatrixHelper.translatePositionToWorldSpace(pose, new Vector3f(0, 0, 0), state.cameraPose);
        ChainSimulationState sim = state.sim;
        // handle stays at origin in local model space
        sim.prevHandlePos.set(sim.handlePos);
        sim.handlePos.set(currentPos.x, currentPos.y, currentPos.z);

        // gravity
        //Vector3f gravity = DynamicModel.translateVectorToWorldSpace(pose, down.mul(7));
        boolean ui = c.equals(ItemDisplayContext.GUI) ||
                     c.equals(ItemDisplayContext.FIXED) ||
                     c.equals(ItemDisplayContext.GROUND) ||
                     c.equals(ItemDisplayContext.NONE);

        Level level = entity != null ? entity.level() : Minecraft.getInstance().level;

        VerletIntegrator.runVerlet(level, delta, sim, down, 200, ui, state.cameraPose);
    }

    public static void drawLine(
            PoseStack poseStack,
            MultiBufferSource buffer,
            Vector3f start,
            Vector3f end,
            float r, float g, float b, float a
    ) {
        VertexConsumer vc = buffer.getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();

        vc.addVertex(matrix, (float) start.x, (float) start.y, (float) start.z)
                .setColor(r, g, b, a)
                .setNormal(0, 1, 0);

        vc.addVertex(matrix, (float) end.x, (float) end.y, (float) end.z)
                .setColor(r, g, b, a)
                .setNormal(0, 1, 0);
    }



    /* ---------------- END MATRIX ---------------- */

    @Override
    protected Matrix4f computeEnd(ChainState state, PoseStack poseStack) {

        int count = state.sim.nodes.length;

        // World-space end
        Vector3f endWorld =
                new Vector3f(state.sim.nodes[count - 1].pos);

        // Convert to local/model space
        Vector3f endLocal =
                MatrixHelper.translatePositionToLocalSpace(
                        poseStack, endWorld, state.cameraPose
                );

        ChainSegment last = state.sim.segments[state.sim.segments.length - 1];

        return new Matrix4f()
                .translation(endLocal);

    }




    /* ---------------- RENDER ---------------- */

    @Override
    public void render(
            RenderContext context) {
        context.matrices().pushPose();
        context.matrices().mulPose(transform.toMatrix());
        super.render(context);
        context.matrices().popPose();
    }

    @Override
    protected void renderDynamic(
            RenderContext context, Vector3f start,
            Matrix4f end,
            ChainState state) {
        renderChain(
                context,
                state);
    }

    private static Quaternionf lookRotation(Vector3f forward, Vector3f up) {
        forward.normalize();

        Vector3f right = new Vector3f(up).cross(forward).normalize();
        Vector3f correctedUp = new Vector3f(forward).cross(right);

        Matrix3f m = new Matrix3f(
                right.x(),       right.y(),       right.z(),
                correctedUp.x(), correctedUp.y(), correctedUp.z(),
                forward.x(),     forward.y(),     forward.z()
        );

        return new Quaternionf().setFromNormalized(m);
    }

    private static Quaternionf rotationBetween(Vector3f from, Vector3f to) {
        from.normalize();
        to.normalize();

        float dot = from.dot(to);

        // Nearly identical
        if (dot > 0.9999f) {
            return new Quaternionf();
        }

        // Opposite direction (rare but important)
        if (dot < -0.9999f) {
            Vector3f axis = new Vector3f(0, 1, 0).cross(from);
            if (axis.lengthSquared() < 1e-6f) {
                axis.set(1, 0, 0).cross(from);
            }
            axis.normalize();
            return new Quaternionf().rotateAxis((float) Math.PI, axis);
        }

        Vector3f axis = new Vector3f(from).cross(to).normalize();
        float angle = (float) Math.acos(dot);

        return new Quaternionf().rotateAxis(angle, axis);
    }



    private void renderChain(
            RenderContext context,
            ChainState state
    ) {
        context.matrices().pushPose();

        // Initial direction
        Vector3f p0 = state.sim.nodes[0].pos;
        Vector3f p1 = state.sim.nodes[1].pos;

        Vector3f prevDir = new Vector3f(p1).sub(p0).normalize();

        // Initial orientation (model forward = +Z)
        Quaternionf orientation = new Quaternionf()
                .rotateTo(new Vector3f(0, 0, 1), prevDir);

        for (int i = 0; i < chain.size(); i++) {

            Vector3f a = state.sim.nodes[i].pos;
            Vector3f b = state.sim.nodes[i + 1].pos;

            Vector3f dir = new Vector3f(b).sub(a);
            if (dir.lengthSquared() < 1e-6f) continue;
            dir.normalize();

            // Minimal rotation from previous direction
            Quaternionf delta = rotationBetween(prevDir, dir);
            orientation.mul(delta);

            context.matrices().pushPose();
            renderSegment(
                    context,
                    a,
                    b,
                    orientation,
                    chain.get(i),
                    state.cameraPose
            );
            context.matrices().popPose();

            prevDir.set(dir);
        }

        context.matrices().popPose();
    }



    private void renderSegment(
            RenderContext context,
            Vector3f start,
            Vector3f end,
            Quaternionf orientation,
            ChainEntry entry,
            Vec3 camPos
    ) {
        Vector3f startLocal = MatrixHelper.translatePositionToLocalSpace(
                context.matrices(), start, camPos
        );

        context.matrices().translate(
                startLocal.x(),
                startLocal.y(),
                startLocal.z()
        );

        context.matrices().mulPose(orientation);

        entry.modelData.forEach(model ->
                model.render(context)
        );
    }




    public static class ChainEntry {
        public final float length;
        public final boolean collide;
        public final float gravity;
        public final boolean locked;
        public final List<BakedMiapiModel> modelData;

        public ChainEntry(float length, boolean collide, boolean locked, float gravity, List<BakedMiapiModel> data) {
            this.length = length;
            this.collide = collide;
            this.modelData = data;
            this.gravity = gravity;
            this.locked = locked;
        }
    }
}
