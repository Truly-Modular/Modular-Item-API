package smartin.miapi.client.model.module.dynamic.trail;

import org.joml.Vector3f;
import smartin.miapi.client.model.module.dynamic.SimulationState;

import java.util.ArrayDeque;
import java.util.Deque;

public class TrailState extends SimulationState {

    public static class TrailPoint {
        public final Vector3f worldPos;
        public final Vector3f worldPos2;
        public float age;

        public TrailPoint(Vector3f worldPos, Vector3f worldPos2) {
            this.worldPos = worldPos;
            this.worldPos2 = worldPos2;
            this.age = 0f;
        }
    }

    public final Deque<TrailPoint> points = new ArrayDeque<>();

    public float lastSpawnTime = 0f;

}
