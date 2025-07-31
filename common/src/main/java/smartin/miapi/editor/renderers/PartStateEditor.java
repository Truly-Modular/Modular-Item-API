package smartin.miapi.editor.renderers;

import com.redpxnda.nucleus.math.InterpolateMode;
import com.redpxnda.nucleus.pose.client.HumanoidPoseAnimation;
import imgui.ImGui;
import imgui.type.ImBoolean;
import org.joml.Vector3f;

import java.util.function.Consumer;

import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

public class PartStateEditor {
    private final HumanoidPoseAnimation.PartState multiplier;
    private final Consumer<HumanoidPoseAnimation.PartState> onChange;

    private final Vector3fEditor positionEditor;
    private final Vector3fEditor rotationEditor;
    private final Vector3fEditor scaleEditor;
    private final InterpolateModeEditor interpolateModeEditor;

    private final ImBoolean interpolationEnabled;
    private InterpolateMode lastUsedInterpolation = null;

    public PartStateEditor(HumanoidPoseAnimation.PartState multiplier, Consumer<HumanoidPoseAnimation.PartState> onChange) {
        this.multiplier = multiplier;
        this.onChange = onChange;

        this.positionEditor = new Vector3fEditor("Position", 0.03125f, multiplier.position, v -> triggerChange());

        // Convert rotation from radians to degrees for the editor
        Vector3f degreesVec = new Vector3f(
                (float) toDegrees(multiplier.rotation.x),
                (float) toDegrees(multiplier.rotation.y),
                (float) toDegrees(multiplier.rotation.z)
        );

        this.rotationEditor = new Vector3fEditor("Rotation (deg)", 1f, degreesVec, degrees -> {
            // Convert degrees back to radians
            multiplier.rotation.set(
                    (float) toRadians(degrees.x),
                    (float) toRadians(degrees.y),
                    (float) toRadians(degrees.z)
            );
            triggerChange();
        });

        this.scaleEditor = new Vector3fEditor("Scale", 0.01f, multiplier.scale, v -> triggerChange());

        this.interpolationEnabled = new ImBoolean(multiplier.interpolateMode != null);
        this.lastUsedInterpolation = multiplier.interpolateMode;

        this.interpolateModeEditor = new InterpolateModeEditor(
                multiplier.interpolateMode,
                mode -> {
                    lastUsedInterpolation = mode;
                    if (interpolationEnabled.get()) {
                        multiplier.interpolateMode = mode;
                        triggerChange();
                    }
                }
        );
    }

    public void render() {
        ImGui.text("State");
        positionEditor.render();
        rotationEditor.render();
        scaleEditor.render();

        ImGui.separator();

        if (ImGui.checkbox("Custom Interpolation", interpolationEnabled)) {
            if (interpolationEnabled.get()) {
                // Re-enable last known mode or default
                multiplier.interpolateMode = lastUsedInterpolation != null ? lastUsedInterpolation : null;
                if (lastUsedInterpolation == null) {
                    lastUsedInterpolation = InterpolateMode.LERP;
                    multiplier.interpolateMode = lastUsedInterpolation;
                }
            } else {
                multiplier.interpolateMode = null;
            }
            triggerChange();
        }

        if (interpolationEnabled.get()) {
            interpolateModeEditor.render();
        }
    }

    private void triggerChange() {
        onChange.accept(new HumanoidPoseAnimation.PartState(
                new Vector3f(multiplier.position),
                new Vector3f(multiplier.rotation),
                new Vector3f(multiplier.scale),
                multiplier.interpolateMode
        ));
    }

    public HumanoidPoseAnimation.PartState getMultiplier() {
        return multiplier;
    }
}
