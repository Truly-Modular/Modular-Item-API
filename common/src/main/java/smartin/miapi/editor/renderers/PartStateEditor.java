package smartin.miapi.editor.renderers;

import com.redpxnda.nucleus.pose.client.HumanoidPoseAnimation;
import imgui.ImGui;
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
    }

    public void render() {
        ImGui.text("State");
        positionEditor.render();
        rotationEditor.render();
        scaleEditor.render();

        ImGui.separator();
                /*
        ImGui.text("Interpolation Mode (JSON or null)");

        String initialText = multiplier.interpolateMode != null
                ? multiplier.interpolateMode.toString()
                : "";

        ImString jsonInput = new ImString(initialText, 1024);

        if (ImGui.inputTextMultiline("##interpJson", jsonInput, ImGuiInputTextFlags.AutoSelectAll)) {
            String input = jsonInput.get().trim();
            if (input.isEmpty()) {
                multiplier.interpolateMode = null;
                triggerChange();
            } else {
                try {
                    JsonElement parsed = JsonParser.parseString(input);
                    multiplier.interpolateMode = InterpolateMode.codec.decode(JsonOps.INSTANCE, parsed)
                            .result()
                            .orElseThrow()
                            .getFirst();
                    triggerChange();
                } catch (Exception e) {
                    // Optional: log or display parse error
                }
            }
        }

         */


    }

    private void triggerChange() {
        onChange.accept(new HumanoidPoseAnimation.PartState(
                new Vector3f(multiplier.position),
                new Vector3f(multiplier.rotation),
                new Vector3f(multiplier.scale),
                null
        ));
    }

    public HumanoidPoseAnimation.PartState getMultiplier() {
        return multiplier;
    }
}
