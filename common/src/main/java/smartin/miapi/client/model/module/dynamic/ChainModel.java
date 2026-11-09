package smartin.miapi.client.model.module.dynamic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.module.dynamic.thread.WorldPhysicsAccess;
import smartin.miapi.client.model.module.dynamic.verlet.ChainMover;
import smartin.miapi.client.model.module.dynamic.verlet.ChainNode;
import smartin.miapi.client.model.module.dynamic.verlet.ChainSimulationState;
import smartin.miapi.client.model.module.dynamic.verlet.VerletIntegrator;
import smartin.miapi.item.modular.Transform;

import java.util.List;

public class ChainModel extends DynamicModel<ChainState> {

    private final List<ChainEntry> chain;

    private final float radius;
    public final Transform transform;
    public final boolean debug;
    public float rigidness;
    public int iterations;
    public float fakeDampness;

    public ChainModel(List<ChainEntry> chain, float radius, float rigidness, int iterations, Transform transform, boolean debug, float fakeDampness) {
        this.chain = chain;
        this.radius = radius;
        this.transform = transform;
        this.iterations = iterations;
        this.debug = debug;
        this.rigidness = rigidness;
        this.fakeDampness = Math.clamp(fakeDampness, 0.0f, 0.9f);
    }

    @Override
    protected ChainState createState(Vec3 pos, RenderContext context) {
        return new ChainState(chain, context, pos, DynamicModel.CURRENT_GRAVITY);
    }


    @Override
    public void updatePhysics(ChainState state, LivingEntity entity, double deltaTime, PoseStack pose, Vector3f down, ItemDisplayContext c) {
        if (deltaTime < 1e-10) {
            return;
        }
        Vector3f currentPos = MatrixHelper.translatePositionToWorldSpace(pose, new Vector3f(0, 0, 0), state.cameraPose);
        ChainSimulationState sim = state.sim;

        if (fakeDampness > 0) {
            Vector3f delta = new Vector3f(currentPos).sub(new Vector3f(sim.handlePos));
            Vector3f teleport = new Vector3f(delta).mul(fakeDampness);
            ChainMover.teleportChain(sim, teleport);
        }

        Vector3f fullLastPos = new Vector3f(sim.handlePos);
        boolean ui = c.equals(ItemDisplayContext.GUI) || c.equals(ItemDisplayContext.FIXED) || c.equals(ItemDisplayContext.GROUND) || c.equals(ItemDisplayContext.NONE);
        int minIterations = (int) (currentPos.distance(fullLastPos) / state.sim.segments[0].restLength);

        VerletIntegrator.runVerlet(WorldPhysicsAccess.CLIENT_WORLD, List.of(new VerletIntegrator.ChainUpdater() {
            @Override
            public void apply(ChainSimulationState sim, float deltaPercent) {
                sim.prevHandlePos.set(sim.handlePos);
                fullLastPos.lerp(currentPos, deltaPercent, sim.handlePos);
            }
        }), minIterations * 2, true, deltaTime, 0.98f, rigidness, sim, down, iterations, ui, state.cameraPose, entity);
        ChainMover.resolveTransforms(pose.last().pose(), state);
    }

    public static void drawLine(PoseStack poseStack, MultiBufferSource buffer, Vector3f start, Vector3f end, float r, float g, float b, float a) {
        VertexConsumer vc = buffer.getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();

        vc.addVertex(matrix, (float) start.x, (float) start.y, (float) start.z).setColor(r, g, b, a).setNormal(0, 1, 0);

        vc.addVertex(matrix, (float) end.x, (float) end.y, (float) end.z).setColor(r, g, b, a).setNormal(0, 1, 0);
    }

    @Override
    protected Matrix4f computeEnd(ChainState state, PoseStack poseStack) {
        ChainNode[] nodes = state.sim.nodes;
        if (nodes.length < 2) {
            return new Matrix4f();
        }

        int last = nodes.length - 1;

        Vector3f start = MatrixHelper.translateVectorToLocalSpace(poseStack, nodes[last - 1].pos, state.cameraPose);
        Vector3f end = MatrixHelper.translateVectorToLocalSpace(poseStack, nodes[last].pos, state.cameraPose);
        Vector3f forward = new Vector3f(end).sub(start);

        if (forward.lengthSquared() < 1.0e-8f) {
            return new Matrix4f().translation(end);
        }

        forward.normalize();

        Vector3f up = new Vector3f(0, 1, 0);

        if (Math.abs(forward.dot(up)) > 0.99f) {
            up.set(1, 0, 0);
        }

        Vector3f right = new Vector3f(up).cross(forward).normalize();
        up.set(forward).cross(right).normalize();
        return new Matrix4f(right.x, right.y, right.z, 0, up.x, up.y, up.z, 0, forward.x, forward.y, forward.z, 0, end.x, end.y, end.z, 1);
    }

    @Override
    public void render(RenderContext context) {
        context.matrices().pushPose();
        context.matrices().mulPose(transform.toMatrix());
        super.render(context);
        context.matrices().popPose();
    }

    @Override
    protected void renderDynamic(RenderContext context, Vector3f start, Matrix4f end, ChainState state) {
        renderChainDynForward(context, state);
        Minecraft.getInstance().getProfiler().push("copy test");
        state.copy();
        Minecraft.getInstance().getProfiler().pop();

        Minecraft.getInstance().getProfiler().push("interpolation");
        state.interpolate(2,state);
        Minecraft.getInstance().getProfiler().pop();
    }

    private void renderChainDynForward(RenderContext context, ChainState state) {
        PoseStack matrix = context.matrices();

        for (int i = 0; i < chain.size(); i++) {

            matrix.pushPose();
            try {
                matrix.mulPose(state.linkTransforms[i]);

                if (debug) {
                    drawLine(context.matrices(), context.vertexConsumers(), new Vector3f(0, 0, 0), new Vector3f(0, 0, 1.0f), 1.0f, 1.0f, 1.0f, 1.0f);
                    drawLine(context.matrices(), context.vertexConsumers(), new Vector3f(0, 0, 0), new Vector3f(1.0f, 0, 0), 1.0f, 0.0f, 0f, 1.0f);
                    drawLine(context.matrices(), context.vertexConsumers(), new Vector3f(0, 0, 0), new Vector3f(0, 1.0f, 0), 0f, 0.0f, 1f, 1.0f);
                }

                matrix.scale(10, 10, 10);
                chain.get(i).modelData.forEach(
                        model -> model.render(context)
                );
            } catch (RuntimeException e) {
                Miapi.LOGGER.warn("caught render error", e);
            } finally {
                matrix.popPose();
            }

        }
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
