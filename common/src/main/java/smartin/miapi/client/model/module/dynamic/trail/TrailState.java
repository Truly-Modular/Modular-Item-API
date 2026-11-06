package smartin.miapi.client.model.module.dynamic.trail;

import org.joml.Vector3f;
import smartin.miapi.client.model.module.dynamic.SimulationState;

import java.util.ArrayDeque;
import java.util.Deque;

public class TrailState extends SimulationState<TrailState> {

    @Override
    public TrailState copy() {
        TrailState copy = new TrailState();

        for (TrailPoint point : points) {
            TrailPoint copiedPoint = point.copy();
            copy.points.addLast(copiedPoint);
        }

        copy.lastSpawnTime = lastSpawnTime;
        return copy;
    }

    @Override
    public TrailState interpolate(float delta, TrailState other) {
        //nothing to interpolate.
        return this.copy();
    }

    public static class TrailPoint {
        public final Vector3f worldPos;
        public final Vector3f worldPos2;
        public float age;

        public TrailPoint(Vector3f worldPos, Vector3f worldPos2) {
            this.worldPos = worldPos;
            this.worldPos2 = worldPos2;
            this.age = 0f;
        }

        public TrailPoint copy() {
            TrailPoint copy = new TrailPoint(worldPos, worldPos2);
            copy.age = age;
            return copy;
        }
    }

    public final Deque<TrailPoint> points = new ArrayDeque<>();
    public float lastSpawnTime = 0f;
}