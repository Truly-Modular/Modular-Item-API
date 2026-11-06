package smartin.miapi.client.model.module.dynamic;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.module.dynamic.verlet.ChainNode;
import smartin.miapi.client.model.module.dynamic.verlet.ChainSegment;
import smartin.miapi.client.model.module.dynamic.verlet.ChainSimulationState;

import java.util.List;

public class ChainState extends SimulationState<ChainState> {

    public final ChainSimulationState sim;
    public final Matrix4f lastProjection = new Matrix4f();

    public ChainState(List<ChainModel.ChainEntry> chain, MiapiModel.RenderContext context, Vec3 cameraPos, Vector3f gravity) {
        sim = new ChainSimulationState();

        Vector3f worldOrigin = getWorldOrigin(context, cameraPos);
        Vector3f direction = getInitialDirection(gravity);

        initializeNodes(chain.size() + 1);
        initializeRoot(worldOrigin);
        initializeChain(chain, worldOrigin, direction, context, cameraPos);
        initializeSimulationState(worldOrigin);
    }

    private ChainState(ChainState source) {
        this.sim = copySimulation(source.sim);

        this.lastTime = source.lastTime;
        this.pose = source.pose;
        this.cameraPose = source.cameraPose;

        this.lastProjection.set(source.lastProjection);
    }

    private ChainSimulationState copySimulation(ChainSimulationState source) {
        ChainSimulationState result = new ChainSimulationState();

        result.handlePos.set(source.handlePos);
        result.prevHandlePos.set(source.prevHandlePos);

        result.nodes = new ChainNode[source.nodes.length];

        for (int i = 0; i < source.nodes.length; i++) {
            ChainNode original = source.nodes[i];
            ChainNode copy = new ChainNode();

            copy.pos.set(original.pos);
            copy.prevPos.set(original.prevPos);

            copy.locked = original.locked;
            copy.collide = original.collide;
            copy.gravity = original.gravity;
            copy.entityCollide = original.entityCollide;

            result.nodes[i] = copy;
        }

        result.segments = new ChainSegment[source.segments.length];

        for (int i = 0; i < source.segments.length; i++) {
            ChainSegment original = source.segments[i];

            ChainSegment copy = new ChainSegment(
                    original.a,
                    original.b,
                    original.restLength
            );

            copy.collide = original.collide;

            result.segments[i] = copy;
        }

        return result;
    }

    private Vector3f getWorldOrigin(MiapiModel.RenderContext context, Vec3 cameraPos) {
        return MatrixHelper.translateVectorToWorldSpace(context.matrices(), new Vector3f(), cameraPos);
    }

    private Vector3f getInitialDirection(Vector3f gravity) {
        Vector3f direction = new Vector3f(gravity);

        if (direction.lengthSquared() < 1.0e-6f) {
            direction.set(0, -1, 0);
        } else {
            direction.normalize();
        }

        return direction;
    }

    private void initializeNodes(int count) {
        sim.nodes = new ChainNode[count];

        for (int i = 0; i < count; i++) {
            sim.nodes[i] = new ChainNode();
        }
    }

    private void initializeRoot(Vector3f worldOrigin) {
        ChainNode root = sim.nodes[0];

        root.locked = true;
        root.pos.set(worldOrigin);
        root.prevPos.set(worldOrigin);
    }

    private void initializeChain(List<ChainModel.ChainEntry> chain, Vector3f worldOrigin, Vector3f direction, MiapiModel.RenderContext context, Vec3 cameraPos) {
        sim.segments = new ChainSegment[chain.size()];

        Vector3f cursor = new Vector3f(worldOrigin);

        float worldLengthScale = getWorldLengthScale(context, cameraPos);

        for (int i = 0; i < chain.size(); i++) {
            ChainModel.ChainEntry entry = chain.get(i);

            ChainNode node = sim.nodes[i + 1];

            float worldLength = entry.length * worldLengthScale;

            node.locked = entry.locked;
            node.collide = entry.collide;
            node.gravity = entry.gravity;

            cursor.fma(worldLength, direction);

            node.pos.set(cursor);
            node.prevPos.set(cursor);

            ChainSegment segment = new ChainSegment(i, i + 1, worldLength);

            segment.collide = entry.collide;

            sim.segments[i] = segment;
        }
    }

    private float getWorldLengthScale(MiapiModel.RenderContext context, Vec3 cameraPos) {
        Vector3f origin = MatrixHelper.translateVectorToWorldSpace(context.matrices(), new Vector3f(), cameraPos);

        Vector3f offset = MatrixHelper.translateVectorToWorldSpace(context.matrices(), new Vector3f(1, 0, 0), cameraPos);

        return offset.sub(origin).length();
    }

    private void initializeSimulationState(Vector3f worldOrigin) {
        sim.handlePos.set(worldOrigin);
        sim.prevHandlePos.set(worldOrigin);
    }

    @Override
    public ChainState copy() {
        return new ChainState(this);
    }

    @Override
    public ChainState interpolate(float delta, ChainState other) {
        ChainState result = copy();

        if (sim.nodes.length != other.sim.nodes.length) {
            return result;
        }

        delta = Math.clamp(delta, 0.0f, 1.0f);

        result.sim.handlePos.lerp(other.sim.handlePos, delta);

        result.sim.prevHandlePos.lerp(
                other.sim.prevHandlePos,
                delta
        );

        for (int i = 0; i < sim.nodes.length; i++) {
            result.sim.nodes[i].pos.lerp(
                    other.sim.nodes[i].pos,
                    delta
            );

            result.sim.nodes[i].prevPos.lerp(
                    other.sim.nodes[i].prevPos,
                    delta
            );
        }

        result.lastProjection.lerp(
                other.lastProjection,
                delta
        );

        return result;
    }
}
