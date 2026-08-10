package smartin.miapi.editor.pose;

import imgui.ImGui;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class Vector3fEditor {
    private final String label;
    private final Vector3f value;
    private final float changeRate;
    private final Consumer<Vector3f> onChange;
    private final int hashCode= System.identityHashCode(this);

    public Vector3fEditor(String label,float changeRate, Vector3f value, Consumer<Vector3f> onChange) {
        this.label = label;
        this.value = value;
        this.onChange = onChange;
        this.changeRate = changeRate;
    }

    public void render() {
        float[] arr = new float[]{value.x(), value.y(), value.z()};
        if (ImGui.dragFloat3(label, arr,changeRate)) {
            value.set(arr[0], arr[1], arr[2]);
            onChange.accept(new Vector3f(value)); // new copy
        }
    }

    public Vector3f getValue() {
        return value;
    }
}
