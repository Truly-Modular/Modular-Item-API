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
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import smartin.miapi.client.model.MiapiModel;
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
    public float rigidness;
    public int iterations;

    public ChainModel(
            List<ChainEntry> chain,
            float radius,
            float rigidness,
            int iterations,
            Transform transform,
            boolean debug
    ) {
        this.chain = chain;
        this.radius = radius;
        this.transform = transform;
        this.iterations = iterations;
        this.debug = debug;
        this.rigidness = rigidness;

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
            Vector3f localOrigin = new Vector3f(0, 0, 0);
            Vector3f worldOrigin = MatrixHelper.translateVectorToWorldSpace(context.matrices(), localOrigin, cameraPos);

            for (int i = 0; i < nodeCount; i++) {
                sim.nodes[i] = new ChainNode();
                sim.nodes[i].pos.set(worldOrigin);
                sim.nodes[i].prevPos.set(worldOrigin);
            }
            sim.nodes[0].locked = true;

            Vector3f cursor = new Vector3f(worldOrigin);

            for (int i = 0; i < chain.size(); i++) {
                ChainEntry e = chain.get(i);
                ChainNode next = sim.nodes[i + 1];

                next.locked = e.locked;
                next.collide = e.collide;
                next.gravity = e.gravity;

                float dir = (i % 2 == 0) ? 1f : -1f;
                Vector3f localOffset = new Vector3f(0, e.length * dir, 0);
                Vector3f worldOffset =
                        MatrixHelper.translateVectorToWorldSpace(context.matrices(), localOffset, cameraPos)
                                .sub(worldOrigin);

                cursor.add(worldOffset);
                next.pos.set(cursor);
                next.prevPos.set(cursor);
            }
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

    public final Vector3f prevRootDelta = new Vector3f();
    public final Vector3f rootDelta = new Vector3f();
    public final Vector3f rootAcceleration = new Vector3f();

    @Override
    public void updatePhysics(
            ChainState state,
            LivingEntity entity,
            float deltaTime,
            PoseStack pose,
            Vector3f down,
            ItemDisplayContext c
    ) {
        if (deltaTime < 1e-10) {
            return;
        }
        Vector3f currentPos = MatrixHelper.translatePositionToWorldSpace(
                pose,
                new Vector3f(0, 0, 0),
                state.cameraPose
        );
        ChainSimulationState sim = state.sim;
        Vector3f fullLastPos = new Vector3f(sim.handlePos);

        boolean ui = c.equals(ItemDisplayContext.GUI) ||
                     c.equals(ItemDisplayContext.FIXED) ||
                     c.equals(ItemDisplayContext.GROUND) ||
                     c.equals(ItemDisplayContext.NONE);

        Level level = entity != null ? entity.level() : Minecraft.getInstance().level;
        int minIterations = (int) (currentPos.distance(fullLastPos) / state.sim.segments[0].restLength);

        VerletIntegrator.runVerlet(
                level,
                List.of(new VerletIntegrator.ChainUpdater() {
                    @Override
                    public void apply(ChainSimulationState sim, float deltaPercent) {
                        sim.prevHandlePos.set(sim.handlePos);
                        fullLastPos.lerp(currentPos, deltaPercent, sim.handlePos);

                        //Vector3f delta = new Vector3f(sim.handlePos).sub(sim.prevHandlePos);
                        //Miapi.LOGGER.info("delta step " + delta.length());


                        // delta based on smoothed motion

                        // apply to root
                        //ChainNode root = sim.nodes[0];
                        //root.pos.add(delta);
                        //root.prevPos.add(delta);
                    }
                }),
                minIterations * 2,
                true,
                deltaTime,
                0.98f,
                rigidness,
                sim,
                down,
                iterations,
                ui,
                state.cameraPose
        );
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
        Vector3f endWorld =
                new Vector3f(state.sim.nodes[count - 1].pos);
        Vector3f endLocal =
                MatrixHelper.translatePositionToLocalSpace(
                        poseStack, endWorld, state.cameraPose
                );

        ChainSegment last = state.sim.segments[state.sim.segments.length - 1];

        return new Matrix4f()
                .translation(endLocal);

    }

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
        renderChainDynForward(
                context,
                state);
    }

    private void renderChainSimpleForward(
            RenderContext context,
            ChainState state
    ) {
        context.matrices().pushPose();
        for (int i = 0; i < chain.size(); i++) {
            Vector3f a = MatrixHelper.translateVectorToLocalSpace(context.matrices(), state.sim.nodes[i].pos, state.cameraPose);
            Vector3f b = MatrixHelper.translateVectorToLocalSpace(context.matrices(), state.sim.nodes[i + 1].pos, state.cameraPose);

            Vector3f delta = new Vector3f(b).sub(a);
            /*
            float yaw =
                    (float) Math.atan2(delta.x, delta.z);

            float pitch =
                    (float) Math.atan2(
                            delta.y,
                            Math.sqrt(delta.x * delta.x +
                                      delta.z * delta.z)
                    );
            context.matrices().pushPose();
            context.matrices().translate(a.x, a.y, a.z);
            context.matrices().mulPose(Axis.YP.rotation(yaw));
            context.matrices().mulPose(Axis.XP.rotation(-pitch));
            chain.get(i).modelData.forEach(model ->
                    model.render(context)
            );

            context.matrices().popPose();
            */
            context.matrices().pushPose();
            context.matrices().translate(a.x, a.y, a.z);
            Vector3f dir = new Vector3f(delta).normalize();
            Quaternionf rotation = new Quaternionf()
                    .rotationTo(
                            new Vector3f(0, 0, 1),
                            dir
                    );
            context.matrices().mulPose(rotation);
            chain.get(i).modelData.forEach(model ->
                    model.render(context)
            );
            context.matrices().popPose();
        }
        context.matrices().popPose();
    }


    private void renderChainDynForward(
            RenderContext context,
            ChainState state
    ) {
        Vector3f prevUp = new Vector3f(0, 1, 0);

        context.matrices().pushPose();
        for (int i = 0; i < chain.size(); i++) {
            Vector3f a = MatrixHelper.translateVectorToLocalSpace(context.matrices(), state.sim.nodes[i].pos, state.cameraPose);
            Vector3f b = MatrixHelper.translateVectorToLocalSpace(context.matrices(), state.sim.nodes[i + 1].pos, state.cameraPose);

            Vector3f delta = new Vector3f(b).sub(a);
            Vector3f forward = new Vector3f(delta).normalize();

            // project previous up onto plane perpendicular to forward
            Vector3f up = new Vector3f(prevUp)
                    .sub(new Vector3f(forward)
                            .mul(prevUp.dot(forward)));

            // degeneracy fallback
            if (up.lengthSquared() < 1e-6f) {
                up = Math.abs(forward.y) > 0.99f
                        ? new Vector3f(1, 0, 0)
                        : new Vector3f(0, 1, 0);

                up.sub(new Vector3f(forward)
                        .mul(up.dot(forward)));
            }

            up.normalize();

            Vector3f right = new Vector3f(up)
                    .cross(forward)
                    .normalize();

            up = new Vector3f(forward)
                    .cross(right)
                    .normalize();

            prevUp.set(up);


            Matrix4f basisOld = new Matrix4f(
                    right.x, up.x, forward.x, 0,
                    right.y, up.y, forward.y, 0,
                    right.z, up.z, forward.z, 0,
                    0, 0, 0, 1
            );

            Matrix4f basis = new Matrix4f(
                    right.x, right.y, right.z, 0,
                    up.x, up.y, up.z, 0,
                    forward.x, forward.y, forward.z, 0,
                    0, 0, 0, 1
            );

            context.matrices().pushPose();
            context.matrices().translate(a.x, a.y, a.z);
            context.matrices().mulPose(basis);
            float len = delta.length();
            float sidelength = len;
            float targetLength = chain.get(i).length;
            float diff = targetLength - len;
            if (diff != 0) {
                sidelength = targetLength + diff / 10;
            }
            context.matrices().scale(sidelength, sidelength, len);
            if (debug) {
                drawLine(context.matrices(), context.vertexConsumers(),
                        new Vector3f(0, 0, 0), new Vector3f(0, 0, 1.0f),
                        1.0f, 1.0f, 1.0f, 1.0f);
                drawLine(context.matrices(), context.vertexConsumers(),
                        new Vector3f(0, 0, 0), new Vector3f(1.0f, 0, 0),
                        1.0f, 0.0f, 0f, 1.0f);
                drawLine(context.matrices(), context.vertexConsumers(),
                        new Vector3f(0, 0, 0), new Vector3f(0, 1.0f, 0),
                        0f, 0.0f, 1f, 1.0f);
            }
            context.matrices().scale(10, 10, 10);
            chain.get(i).modelData.forEach(model ->
                    model.render(context)
            );
            //context.matrices().scale(0.1f,0.1f,0.1f);
            context.matrices().popPose();
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
        public final List<MiapiModel> modelData;

        public ChainEntry(float length, boolean collide, boolean locked, float gravity, List<MiapiModel> data) {
            this.length = length;
            this.collide = collide;
            this.modelData = data;
            this.gravity = gravity;
            this.locked = locked;
        }
    }
}
