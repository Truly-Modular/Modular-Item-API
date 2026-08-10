package smartin.miapi.editor.renderers;

import com.redpxnda.nucleus.pose.client.HumanoidPoseAnimation;
import imgui.ImGui;
import org.joml.Vector3f;
import smartin.miapi.editor.pose.Vector3fEditor;

import java.util.function.Consumer;

public class FrameMultiplierEditor {
    private final HumanoidPoseAnimation.FrameMultiplier multiplier;
    private final Consumer<HumanoidPoseAnimation.FrameMultiplier> onChange;

    private final Vector3fEditor positionEditor;
    private final Vector3fEditor rotationEditor;
    private final Vector3fEditor scaleEditor;

    public FrameMultiplierEditor(HumanoidPoseAnimation.FrameMultiplier multiplier, Consumer<HumanoidPoseAnimation.FrameMultiplier> onChange) {
        this.multiplier = multiplier;
        this.onChange = onChange;

        this.positionEditor = new Vector3fEditor("Position",0.03125f, multiplier.position, v -> triggerChange());
        this.rotationEditor = new Vector3fEditor("Rotation",1f, multiplier.rotation, v -> triggerChange());
        this.scaleEditor = new Vector3fEditor("Scale",0.01f, multiplier.scale, v -> triggerChange());
    }

    public void render() {
        ImGui.text("FrameMultiplier");
        positionEditor.render();
        rotationEditor.render();
        scaleEditor.render();
    }

    private void triggerChange() {
        onChange.accept(new HumanoidPoseAnimation.FrameMultiplier(
                new Vector3f(multiplier.position),
                new Vector3f(multiplier.rotation),
                new Vector3f(multiplier.scale)
        ));
    }

    public HumanoidPoseAnimation.FrameMultiplier getMultiplier() {
        return multiplier;
    }
}
