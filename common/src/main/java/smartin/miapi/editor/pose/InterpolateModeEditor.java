package smartin.miapi.editor.pose;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.redpxnda.nucleus.math.InterpolateMode;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import smartin.miapi.registries.JsonOpsBooleanPatched;

import java.util.List;
import java.util.function.Consumer;

public class InterpolateModeEditor {
    private final Consumer<InterpolateMode> onChange;
    private InterpolateMode mode;

    private static final List<String> options = List.of(
            "none", "lerp", "cosine", "easeIn", "easeOut", "easeInOut", "custom"
    );

    private final ImInt selectedIndex;
    private final ImString customJson = new ImString(1024);
    private final ImFloat amplifierSlider = new ImFloat(2.0f);

    public InterpolateModeEditor(InterpolateMode initial, Consumer<InterpolateMode> onChange) {
        this.onChange = onChange;
        this.mode = initial;

        String initialKey = getModeKey(initial);
        int idx = options.indexOf(initialKey);
        if (idx == -1) idx = options.size() - 1; // fallback to custom
        selectedIndex = new ImInt(idx);

        if ("custom".equals(initialKey)) {
            try {
                customJson.set(initial.toJson().toString());
            } catch (Exception ignored) {}
        }

        if (initial instanceof InterpolateMode.EaseIn ei) amplifierSlider.set(ei.amplifier());
        else if (initial instanceof InterpolateMode.EaseOut eo) amplifierSlider.set(eo.amplifier());
        else if (initial instanceof InterpolateMode.EaseInOut eio) amplifierSlider.set(eio.amplifier());
    }

    public void render() {
        ImGui.text("Interpolation Mode:");
        if (ImGui.beginCombo("##interpCombo", options.get(selectedIndex.get()), 0)) {
            for (int i = 0; i < options.size(); i++) {
                boolean isSelected = (selectedIndex.get() == i);
                if (ImGui.selectable(options.get(i), isSelected)) {
                    selectedIndex.set(i);
                    updateModeFromSelection();
                }
                if (isSelected) ImGui.setItemDefaultFocus();
            }
            ImGui.endCombo();
        }

        // Render additional controls for ease* types
        String type = options.get(selectedIndex.get());
        if (type.startsWith("ease")) {
            if (ImGui.dragFloat("Amplifier", amplifierSlider.getData(), 0.1f, 0.1f)) {
                switch (type) {
                    case "easeIn" -> mode = new InterpolateMode.EaseIn(amplifierSlider.get());
                    case "easeOut" -> mode = new InterpolateMode.EaseOut(amplifierSlider.get());
                    case "easeInOut" -> mode = new InterpolateMode.EaseInOut(amplifierSlider.get());
                }
                onChange.accept(mode);
            }
        }

        // Render custom JSON mode
        if ("custom".equals(type)) {
            ImGui.inputTextMultiline("##customInterpJson", customJson, ImGuiInputTextFlags.AutoSelectAll);
            if (ImGui.button("Apply Custom Interpolation")) {
                try {
                    JsonElement parsed = JsonParser.parseString(customJson.get().trim());
                    InterpolateMode customMode = InterpolateMode.codec.decode(JsonOpsBooleanPatched.INSTANCE, parsed)
                            .result().orElseThrow().getFirst();
                    mode = customMode;
                    onChange.accept(mode);
                } catch (Exception e) {
                    ImGui.textColored(1, 0, 0, 1, "Invalid JSON");
                }
            }
        }
    }

    public InterpolateMode getMode() {
        return mode;
    }

    private void updateModeFromSelection() {
        String selected = options.get(selectedIndex.get());
        try {
            switch (selected) {
                case "none" -> mode = InterpolateMode.NONE;
                case "lerp" -> mode = InterpolateMode.LERP;
                case "cosine" -> mode = InterpolateMode.COS;
                case "easeIn" -> mode = new InterpolateMode.EaseIn(amplifierSlider.get());
                case "easeOut" -> mode = new InterpolateMode.EaseOut(amplifierSlider.get());
                case "easeInOut" -> mode = new InterpolateMode.EaseInOut(amplifierSlider.get());
                case "custom" -> {
                    // No-op. JSON will be parsed separately when user clicks "Apply"
                    return;
                }
            }
            onChange.accept(mode);
        } catch (Exception e) {
            ImGui.textColored(1, 0, 0, 1, "Failed to apply interpolation mode");
        }
    }

    private String getModeKey(InterpolateMode mode) {
        if (mode == null) return "none";
        try {
            JsonElement json = mode.toJson();
            if (json.isJsonPrimitive()) {
                return json.getAsString();
            } else if (json.isJsonObject() && json.getAsJsonObject().has("type")) {
                return json.getAsJsonObject().get("type").getAsString();
            }
        } catch (Exception ignored) {}
        return "custom";
    }
}
